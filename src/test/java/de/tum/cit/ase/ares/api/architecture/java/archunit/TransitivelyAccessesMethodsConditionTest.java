package de.tum.cit.ase.ares.api.architecture.java.archunit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaCodeUnit;

import de.tum.cit.ase.ares.testutilities.CustomConditionEvents;

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
}
