package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.*;

import java.io.IOException;
import java.nio.file.Path;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import com.tngtech.archunit.core.importer.ClassFileImporter;

import de.tum.cit.ase.ares.testutilities.CustomConditionEvents;
import de.tum.cit.ase.ares.testutilities.JavacFixtures;

/**
 * JUnit 5 tests for TransitivelyAccessesMethodsCondition.
 */
public class TransitivelyAccessesMethodsConditionTest {

	private DescribedPredicate<JavaAccess<?>> violatingPredicate;

	private TransitivelyAccessesMethodsCondition conditionUnderTest;

	@BeforeEach
	void setUp() {
		// A simple predicate that matches any access whose target's name contains
		// "forbidden"
		violatingPredicate = new DescribedPredicate<>("forbidden-method predicate") {
			@Override
			public boolean test(JavaAccess<?> access) {
				AccessTarget target = access.getTarget();
				return target.getFullName().contains("forbidden");
			}
		};

		conditionUnderTest = new TransitivelyAccessesMethodsCondition(violatingPredicate);
	}

	@Test
	void constructor_nullPredicate_throwsException() {
		assertThrows(NullPointerException.class, () -> new TransitivelyAccessesMethodsCondition(null));
	}

	@Test
	void checkCondition_noViolations_doesNotAddEvents() {
		// Arrange: create a JavaClass that doesn't match the violating predicate
		JavaClass clazz = mock(JavaClass.class);

		@SuppressWarnings("unchecked")
		JavaAccess<AccessTarget> nonViolatingAccess = mock(JavaAccess.class);
		AccessTarget allowedTarget = mock(AccessTarget.class);
		when(nonViolatingAccess.getTarget()).thenReturn(allowedTarget);
		when(allowedTarget.getFullName()).thenReturn("com.example.AllowedClass.allowedMethod");

		when(clazz.getAccessesFromSelf()).thenReturn(Set.of(nonViolatingAccess));

		CustomConditionEvents events = new CustomConditionEvents();

		// Act: check the condition
		conditionUnderTest.check(clazz, events);

		// Assert: no violations should be found
		assertThat(events.getViolating()).isEmpty();
	}

	@Test
	void checkCondition_directViolation_addsEvent() {
		// Arrange: create a JavaClass that directly violates the predicate
		JavaClass clazz = mock(JavaClass.class);

		@SuppressWarnings("unchecked")
		JavaAccess<AccessTarget> violatingAccess = mock(JavaAccess.class);
		AccessTarget forbiddenTarget = mock(AccessTarget.class);
		JavaCodeUnit originUnit = mock(JavaCodeUnit.class);

		when(violatingAccess.getTarget()).thenReturn(forbiddenTarget);
		when(violatingAccess.getOrigin()).thenReturn(originUnit);
		when(forbiddenTarget.getFullName()).thenReturn("com.example.SomeClass.forbiddenMethod");
		when(originUnit.getFullName()).thenReturn("com.example.Caller.callMethod");

		when(clazz.getAccessesFromSelf()).thenReturn(Set.of(violatingAccess));

		CustomConditionEvents events = new CustomConditionEvents();

		// Act: check the condition
		conditionUnderTest.check(clazz, events);

		// Assert: a violation should be found
		assertThat(events.getViolating()).isNotEmpty();
	}

	@Test
	void checkCondition_transitiveViolation_addsEvent() {
		JavaClass callerClass = mock(JavaClass.class);
		JavaClass helperClass = mock(JavaClass.class);
		JavaAccess<?> callerToHelper = access("com.example.Caller.call", "com.example.Helper.work", helperClass);
		JavaAccess<?> helperToForbidden = access("com.example.Helper.work", "com.example.Api.forbidden", null);
		when(callerClass.getAccessesFromSelf()).thenReturn(Set.of(callerToHelper));
		when(helperClass.getAccessesFromSelf()).thenReturn(Set.of(helperToForbidden));
		CustomConditionEvents events = new CustomConditionEvents();

		conditionUnderTest.check(callerClass, events);

		assertThat(events.getViolating()).hasSize(1);
	}

	@Test
	void checkCondition_cycleLeadingToViolation_addsEvent() {
		JavaClass callerClass = mock(JavaClass.class);
		JavaClass firstHelperClass = mock(JavaClass.class);
		JavaClass secondHelperClass = mock(JavaClass.class);
		JavaAccess<?> callerToFirst = access("com.example.Caller.call", "com.example.First.work", firstHelperClass);
		JavaAccess<?> firstToSecond = access("com.example.First.work", "com.example.Second.work", secondHelperClass);
		JavaAccess<?> secondToFirst = access("com.example.Second.work", "com.example.First.work", firstHelperClass);
		JavaAccess<?> secondToForbidden = access("com.example.Second.work", "com.example.Api.forbidden", null);
		when(callerClass.getAccessesFromSelf()).thenReturn(Set.of(callerToFirst));
		when(firstHelperClass.getAccessesFromSelf()).thenReturn(Set.of(firstToSecond));
		Set<JavaAccess<?>> secondHelperAccesses = new LinkedHashSet<>();
		secondHelperAccesses.add(secondToFirst);
		secondHelperAccesses.add(secondToForbidden);
		when(secondHelperClass.getAccessesFromSelf()).thenReturn(secondHelperAccesses);
		CustomConditionEvents events = new CustomConditionEvents();

		conditionUnderTest.check(callerClass, events);

		assertThat(events.getViolating()).hasSize(1);
	}

	@Test
	void checkCondition_safeCycle_doesNotAddEvents() {
		JavaClass callerClass = mock(JavaClass.class);
		JavaClass firstHelperClass = mock(JavaClass.class);
		JavaClass secondHelperClass = mock(JavaClass.class);
		JavaAccess<?> callerToFirst = access("com.example.Caller.call", "com.example.First.work", firstHelperClass);
		JavaAccess<?> firstToSecond = access("com.example.First.work", "com.example.Second.work", secondHelperClass);
		JavaAccess<?> secondToFirst = access("com.example.Second.work", "com.example.First.work", firstHelperClass);
		when(callerClass.getAccessesFromSelf()).thenReturn(Set.of(callerToFirst));
		when(firstHelperClass.getAccessesFromSelf()).thenReturn(Set.of(firstToSecond));
		when(secondHelperClass.getAccessesFromSelf()).thenReturn(Set.of(secondToFirst));
		CustomConditionEvents events = new CustomConditionEvents();

		conditionUnderTest.check(callerClass, events);

		assertThat(events.getViolating()).isEmpty();
	}

	@Test
	void checkCondition_sharedTarget_indexesTargetClassOnce() {
		JavaClass callerClass = mock(JavaClass.class);
		JavaClass helperClass = mock(JavaClass.class);
		JavaAccess<?> firstAccess = access("com.example.Caller.first", "com.example.Helper.work", helperClass);
		JavaAccess<?> secondAccess = access("com.example.Caller.second", "com.example.Helper.work", helperClass);
		Set<JavaAccess<?>> callerAccesses = new LinkedHashSet<>();
		callerAccesses.add(firstAccess);
		callerAccesses.add(secondAccess);
		when(callerClass.getAccessesFromSelf()).thenReturn(callerAccesses);
		when(helperClass.getAccessesFromSelf()).thenReturn(Set.of());
		CustomConditionEvents events = new CustomConditionEvents();

		conditionUnderTest.check(callerClass, events);

		assertThat(events.getViolating()).isEmpty();
		verify(helperClass, times(1)).getAccessesFromSelf();
	}

	@Test
	void init_clearsResultsFromPreviousEvaluation() {
		JavaClass callerClass = mock(JavaClass.class);
		JavaClass helperClass = mock(JavaClass.class);
		JavaAccess<?> callerToHelper = access("com.example.Caller.call", "com.example.Helper.work", helperClass);
		JavaAccess<?> helperToForbidden = access("com.example.Helper.work", "com.example.Api.forbidden", null);
		when(callerClass.getAccessesFromSelf()).thenReturn(Set.of(callerToHelper));
		when(helperClass.getAccessesFromSelf()).thenReturn(Set.of(), Set.of(helperToForbidden));
		CustomConditionEvents firstEvents = new CustomConditionEvents();
		CustomConditionEvents secondEvents = new CustomConditionEvents();

		conditionUnderTest.check(callerClass, firstEvents);
		conditionUnderTest.init(List.of(callerClass));
		conditionUnderTest.check(callerClass, secondEvents);

		assertThat(firstEvents.getViolating()).isEmpty();
		assertThat(secondEvents.getViolating()).hasSize(1);
	}

	@SuppressWarnings("unchecked")
	private static JavaAccess<?> access(String originName, String targetName, JavaClass targetOwner) {
		JavaAccess<AccessTarget> access = mock(JavaAccess.class);
		AccessTarget target = mock(AccessTarget.class);
		JavaCodeUnit origin = mock(JavaCodeUnit.class);
		when(access.getOrigin()).thenReturn(origin);
		when(access.getTarget()).thenReturn(target);
		when(access.getTargetOwner()).thenReturn(targetOwner);
		when(origin.getFullName()).thenReturn(originName);
		when(target.getFullName()).thenReturn(targetName);
		return access;
	}

	/**
	 * The full name of the one method of this signature the target forbids.
	 */
	private static final String FILE_DELETE = "java.io.File.delete()";

	/**
	 * A method that deletes the file it is given.
	 */
	private static final String DIRECT = """
			package p;
			public class Direct {
				public boolean run(java.io.File f) {
					return f.delete();
				}
			}
			""";

	/**
	 * The first method of a chain of three helpers that ends in a delete.
	 */
	private static final String ENTRY = """
			package p;
			public class Entry {
				public static boolean m1(java.io.File f) {
					return Helpers.h1(f);
				}
			}
			""";

	/**
	 * The three helpers, each calling the next, the last one deleting.
	 */
	private static final String HELPERS = """
			package p;
			public class Helpers {
				public static boolean h1(java.io.File f) {
					return h2(f);
				}
				public static boolean h2(java.io.File f) {
					return h3(f);
				}
				public static boolean h3(java.io.File f) {
					return f.delete();
				}
			}
			""";

	/**
	 * A helper that deletes, called by three different callers.
	 */
	private static final String HELPER = """
			package p;
			public class Helper {
				public static boolean helper(java.io.File f) {
					return f.delete();
				}
			}
			""";

	/**
	 * A caller that reaches the helper directly.
	 */
	private static final String PRIMER = """
			package p;
			public class Primer {
				public static boolean a(java.io.File f) {
					return Helper.helper(f);
				}
			}
			""";

	/**
	 * Another caller that reaches the helper directly.
	 */
	private static final String SECOND = """
			package p;
			public class Second {
				public static boolean b(java.io.File f) {
					return Helper.helper(f);
				}
			}
			""";

	/**
	 * A caller that reaches the helper through a method of its own class.
	 */
	private static final String MIDDLE = """
			package p;
			public class Middle {
				public static boolean c(java.io.File f) {
					return mid(f);
				}
				public static boolean mid(java.io.File f) {
					return Helper.helper(f);
				}
			}
			""";

	/**
	 * A caller whose helper deletes in the first version of the fixture.
	 */
	private static final String CALLER = """
			package p;
			public class Caller {
				public static boolean run(java.io.File f) {
					return Helper.m(f);
				}
			}
			""";

	/**
	 * The helper of the first version, which deletes.
	 */
	private static final String DELETING_HELPER = """
			package p;
			public class Helper {
				public static boolean m(java.io.File f) {
					return f.delete();
				}
			}
			""";

	/**
	 * The helper of the second version, which does nothing.
	 */
	private static final String EMPTY_HELPER = """
			package p;
			public class Helper {
				public static boolean m(java.io.File f) {
					return false;
				}
			}
			""";

	/**
	 * A method that asks for a temporary file without naming a directory.
	 */
	private static final String TEMPORARY = """
			package p;
			public class Temporary {
				public java.io.File run() throws java.io.IOException {
					return java.io.File.createTempFile("a", "b");
				}
			}
			""";

	/**
	 * A method that asks for a temporary file in a directory it names.
	 */
	private static final String NAMED_DIRECTORY = """
			package p;
			public class NamedDirectory {
				public java.io.File run(java.io.File directory) throws java.io.IOException {
					return java.io.File.createTempFile("a", "b", directory);
				}
			}
			""";

	/**
	 * Builds a predicate that matches exactly the calls of {@code File.delete()}.
	 *
	 * @return the predicate
	 */
	private static DescribedPredicate<JavaAccess<?>> callsFileDelete() {
		return new DescribedPredicate<>("calls File.delete") {
			/** Tells whether the access targets {@code File.delete()}. */
			@Override
			public boolean test(JavaAccess<?> access) {
				return FILE_DELETE.equals(access.getTarget().getFullName());
			}
		};
	}

	/**
	 * Compiles the sources with {@code javac} below the directory and imports the
	 * class files.
	 *
	 * @param directory the directory to work in
	 * @param sources   the source files, by their path below the source folder
	 * @return the imported classes
	 * @throws IOException if a source file cannot be written
	 */
	private static JavaClasses compileAndImport(Path directory, Map<String, String> sources) throws IOException {
		return new ClassFileImporter().importPath(JavacFixtures.compileSourceTexts(directory, sources));
	}

	/**
	 * Returns the full name ArchUnit gives to the only method of that name in a
	 * class.
	 *
	 * @param classes   the imported classes
	 * @param className the class
	 * @param method    the method name
	 * @return the full name of the method
	 */
	private static String fullName(JavaClasses classes, String className, String method) {
		return classes.get(className).getMethods().stream().filter(candidate -> candidate.getName().equals(method))
				.findFirst().orElseThrow().getFullName();
	}

	/**
	 * Returns the description lines of every event the condition added.
	 *
	 * @param events the events
	 * @return the description lines
	 */
	private static List<String> messages(CustomConditionEvents events) {
		return events.getViolating().stream().flatMap(event -> event.getDescriptionLines().stream()).toList();
	}

	/**
	 * Checks the class and returns the messages of the events it produced.
	 *
	 * @param condition the condition, already initialised
	 * @param classes   the imported classes
	 * @param className the class to check
	 * @return the event messages
	 */
	private static List<String> check(TransitivelyAccessesMethodsCondition condition, JavaClasses classes,
			String className) {
		CustomConditionEvents events = new CustomConditionEvents();
		condition.check(classes.get(className), events);
		return messages(events);
	}

	/**
	 * Picks the one message that starts at the given method.
	 *
	 * @param messages the messages
	 * @param method   the full name of the calling method
	 * @return the message
	 */
	private static String messageOf(List<String> messages, String method) {
		List<String> matching = messages.stream().filter(message -> message.contains("Method <" + method + ">"))
				.toList();
		assertEquals(1, matching.size(), messages::toString);
		return matching.get(0);
	}

	/**
	 * A method that deletes the file itself is reported as a plain access, without
	 * a chain.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void describesADirectAccessWithoutAChain(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, Map.of("p/Direct.java", DIRECT));
		TransitivelyAccessesMethodsCondition condition = new TransitivelyAccessesMethodsCondition(callsFileDelete());
		condition.init(classes);

		String message = messageOf(check(condition, classes, "p.Direct"), fullName(classes, "p.Direct", "run"));

		assertTrue(message.contains("accesses <java.io.File.delete()>"), message);
		assertFalse(message.contains("transitively"), message);
		assertFalse(message.contains(" by ["), message);
	}

	/**
	 * A chain of calls is reported in the order the calls happen, from the caller
	 * to the method that deletes.
	 *
	 * @param directory the directory the fixtures are compiled in
	 * @throws IOException if a fixture cannot be written
	 */
	@Test
	void describesATransitiveAccessWithItsChainInCallOrder(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, Map.of("p/Entry.java", ENTRY, "p/Helpers.java", HELPERS));
		TransitivelyAccessesMethodsCondition condition = new TransitivelyAccessesMethodsCondition(callsFileDelete());
		condition.init(classes);
		String m1 = fullName(classes, "p.Entry", "m1");
		String chain = m1 + "->" + fullName(classes, "p.Helpers", "h1") + "->" + fullName(classes, "p.Helpers", "h2")
				+ "->" + fullName(classes, "p.Helpers", "h3");

		String message = messageOf(check(condition, classes, "p.Entry"), m1);

		assertTrue(message.contains("transitively accesses <java.io.File.delete()> by [" + chain + "]"), message);
	}

	/**
	 * A caller checked after another one reuses what the first one learnt about the
	 * shared helper, and still reports its own chain.
	 *
	 * @param directory the directory the fixtures are compiled in
	 * @throws IOException if a fixture cannot be written
	 */
	@Test
	void reusesTheVerdictOfAHelperCheckedBefore(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, Map.of("p/Helper.java", HELPER, "p/Primer.java", PRIMER,
				"p/Second.java", SECOND, "p/Middle.java", MIDDLE));
		TransitivelyAccessesMethodsCondition condition = new TransitivelyAccessesMethodsCondition(callsFileDelete());
		condition.init(classes);
		String helper = fullName(classes, "p.Helper", "helper");
		String b = fullName(classes, "p.Second", "b");
		String c = fullName(classes, "p.Middle", "c");
		String mid = fullName(classes, "p.Middle", "mid");

		check(condition, classes, "p.Primer");
		List<String> second = check(condition, classes, "p.Second");
		List<String> middle = check(condition, classes, "p.Middle");

		assertTrue(
				messageOf(second, b)
						.contains("transitively accesses <java.io.File.delete()> by [" + b + "->" + helper + "]"),
				second::toString);
		assertTrue(
				messageOf(middle, c).contains(
						"transitively accesses <java.io.File.delete()> by [" + c + "->" + mid + "->" + helper + "]"),
				middle::toString);
	}

	/**
	 * A second run over other classes starts from nothing: a helper that deleted in
	 * the first run and does not in the second leaves no trace.
	 *
	 * @param directory the directory the fixtures are compiled in
	 * @throws IOException if a fixture cannot be written
	 */
	@Test
	void forgetsTheVerdictsOfThePreviousRun(@TempDir Path directory) throws IOException {
		JavaClasses before = compileAndImport(directory.resolve("v1"),
				Map.of("p/Caller.java", CALLER, "p/Helper.java", DELETING_HELPER));
		JavaClasses after = compileAndImport(directory.resolve("v2"),
				Map.of("p/Caller.java", CALLER, "p/Helper.java", EMPTY_HELPER));
		TransitivelyAccessesMethodsCondition condition = new TransitivelyAccessesMethodsCondition(callsFileDelete());

		condition.init(before);
		List<String> first = check(condition, before, "p.Caller");
		condition.init(after);
		List<String> second = check(condition, after, "p.Caller");

		assertEquals(1, first.size(), first::toString);
		assertEquals(List.of(), second);
	}

	/**
	 * The verdict on one access is forgotten between runs: the same classes are
	 * reported in one run and not in the next once the predicate changes its mind.
	 *
	 * @param directory the directory the fixture is compiled in
	 * @throws IOException if the fixture cannot be written
	 */
	@Test
	void forgetsTheVerdictOfAnAccessBetweenRuns(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory, Map.of("p/Direct.java", DIRECT));
		AtomicBoolean forbidden = new AtomicBoolean(true);
		TransitivelyAccessesMethodsCondition condition = new TransitivelyAccessesMethodsCondition(
				new DescribedPredicate<JavaAccess<?>>("calls File.delete while forbidden") {
					/**
					 * Tells whether the access targets {@code File.delete()} while it is forbidden.
					 */
					@Override
					public boolean test(JavaAccess<?> access) {
						return forbidden.get() && FILE_DELETE.equals(access.getTarget().getFullName());
					}
				});

		condition.init(classes);
		List<String> whileForbidden = check(condition, classes, "p.Direct");
		forbidden.set(false);
		condition.init(classes);
		List<String> afterwards = check(condition, classes, "p.Direct");

		assertEquals(1, whileForbidden.size(), whileForbidden::toString);
		assertEquals(List.of(), afterwards);
	}

	/**
	 * A temporary file without a named directory stays allowed even when the
	 * predicate matches every call on {@code java.io.File}, and one with a named
	 * directory is still reported.
	 *
	 * @param directory the directory the fixtures are compiled in
	 * @throws IOException if a fixture cannot be written
	 */
	@Test
	void keepsTheTemporaryFileCallsAllowed(@TempDir Path directory) throws IOException {
		JavaClasses classes = compileAndImport(directory,
				Map.of("p/Temporary.java", TEMPORARY, "p/NamedDirectory.java", NAMED_DIRECTORY));
		TransitivelyAccessesMethodsCondition condition = new TransitivelyAccessesMethodsCondition(
				new DescribedPredicate<JavaAccess<?>>("calls anything on File") {
					/** Tells whether the access targets a member of {@code java.io.File}. */
					@Override
					public boolean test(JavaAccess<?> access) {
						return "java.io.File".equals(access.getTarget().getOwner().getFullName());
					}
				});
		condition.init(classes);

		List<String> temporary = check(condition, classes, "p.Temporary");
		List<String> named = check(condition, classes, "p.NamedDirectory");

		assertEquals(List.of(), temporary);
		assertEquals(1, named.size(), named::toString);
	}
}
