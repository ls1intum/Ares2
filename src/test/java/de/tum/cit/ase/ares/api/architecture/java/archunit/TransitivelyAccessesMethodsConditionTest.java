package de.tum.cit.ase.ares.api.architecture.java.archunit;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.AccessTarget;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaCodeUnit;
import de.tum.cit.ase.ares.testutilities.CustomConditionEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

/**
 * JUnit 5 tests for TransitivelyAccessesMethodsCondition.
 */
public class TransitivelyAccessesMethodsConditionTest {

    private DescribedPredicate<JavaAccess<?>> violatingPredicate;

    private TransitivelyAccessesMethodsCondition conditionUnderTest;

    @BeforeEach
    void setUp() {
        // A simple predicate that matches any access whose target's name contains "forbidden"
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
        assertThrows(
            NullPointerException.class,
            () -> new TransitivelyAccessesMethodsCondition(null)
        );
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
}