package de.tum.cit.ase.ares.api.aop.java.instrumentation;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.instrument.Instrumentation;
import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;

import net.bytebuddy.description.type.TypeDescription;
import net.bytebuddy.matcher.ElementMatcher;

import de.tum.cit.ase.ares.api.aop.java.instrumentation.pointcut.JavaInstrumentationPointcutDefinitions;

/**
 * Activates the real agent of this test JVM and checks that every loaded class
 * a pointcut watches went through a transformer. This is a coverage check: it
 * does not show that each pointcut's advice took effect, which the
 * instrumentation integration tests show by running forbidden code. Classes are
 * told apart by name and loader identity hash. It leaves the agent activated.
 */
class JavaInstrumentationAgentEscapeTest {

	/**
	 * After the real agent of this JVM is activated, every loaded class that a
	 * pointcut watches has been transformed, including the classes loaded before
	 * the activation.
	 *
	 * @throws ReflectiveOperationException If the agent state cannot be read.
	 */
	@Test
	void noWatchedClassEscapesActivation() throws ReflectiveOperationException {
		Instrumentation instrumentation = instrumentation();
		assertNotNull(instrumentation, "the test JVM must run with the Ares agent");

		JavaInstrumentationAgent.activate();

		List<ElementMatcher<TypeDescription>> matchers = JavaInstrumentationAgent.guardedPointcuts().stream()
				.map(group -> JavaInstrumentationPointcutDefinitions.getClassesMatcher(group.methodsMap())).toList();
		List<String> escaped = new ArrayList<>();
		int watched = 0;
		for (Class<?> type : instrumentation.getAllLoadedClasses()) {
			if (!instrumentation.isModifiableClass(type) || type.isHidden()) {
				continue;
			}
			TypeDescription description = TypeDescription.ForLoadedType.of(type);
			if (JavaInstrumentationAgent.ignoredTypes().matches(description)
					|| matchers.stream().noneMatch(matcher -> matcher.matches(description))) {
				continue;
			}
			watched++;
			if (!JavaInstrumentationAgent.wasTransformed(type)) {
				escaped.add(type.getName());
			}
		}
		assertTrue(watched > 0, "no watched class is loaded, so the check proves nothing");
		assertEquals(List.of(), escaped, "watched classes left untransformed");
	}

	/**
	 * Reads the instrumentation the agent received at start-up.
	 *
	 * @return The instrumentation, or null without the agent.
	 * @throws ReflectiveOperationException If the field cannot be read.
	 */
	private static Instrumentation instrumentation() throws ReflectiveOperationException {
		Field field = JavaInstrumentationAgent.class.getDeclaredField("instrumentation");
		field.setAccessible(true);
		return (Instrumentation) field.get(null);
	}
}
