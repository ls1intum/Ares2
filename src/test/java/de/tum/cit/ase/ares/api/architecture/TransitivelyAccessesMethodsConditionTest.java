package de.tum.cit.ase.ares.api.architecture;

import com.tngtech.archunit.base.DescribedPredicate;
import com.tngtech.archunit.core.domain.JavaAccess;
import com.tngtech.archunit.core.domain.JavaClass;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;
import com.tngtech.archunit.lang.SimpleConditionEvent;
import de.tum.cit.ase.ares.api.architecture.java.archunit.postcompile.TransitivelyAccessesMethodsCondition;
import de.tum.cit.ase.ares.testutilities.CustomConditionEvents;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Collection;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TransitivelyAccessesMethodsConditionTest {

    private DescribedPredicate<JavaAccess<?>> predicate;
    private TransitivelyAccessesMethodsCondition condition;
    private JavaClass javaClass;
    private ConditionEvents events;

    @BeforeEach
    void setUp() {
        predicate = new DescribedPredicate<>("test predicate") {
            @Override
            public boolean test(JavaAccess<?> input) {
                // Define your predicate logic here
                return input.getTarget().getFullName().startsWith("java.lang.System");
            }
        };
        condition = new TransitivelyAccessesMethodsCondition(predicate);
        javaClass = new ClassFileImporter().importPackages("de.tum.cit.ase.ares.api.architecture.com.example").stream()
                .filter(cls -> cls.getName().equals("de.tum.cit.ase.ares.api.architecture.com.example.ExampleClass"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));
        events = new CustomConditionEvents();
    }

    @Test
    void testCheckWithMatchingAccess() {
        condition.check(javaClass, events);

        Collection<ConditionEvent> eventsList = events.getViolating();
        assertThat(eventsList).isNotEmpty();
        assertThat(eventsList.iterator().next()).isInstanceOfAny(SimpleConditionEvent.class);
        assertThat(eventsList.iterator().next().isViolation()).isFalse();
    }

    @Test
    void testCheckWithNonMatchingAccess() {
        // Modify the predicate to ensure no matches
        predicate = new DescribedPredicate<>("non-matching predicate") {
            @Override
            public boolean test(JavaAccess<?> input) {
                return false;
            }
        };
        condition = new TransitivelyAccessesMethodsCondition(predicate);

        condition.check(javaClass, events);

        assertThat(events.getViolating()).isEmpty();
    }

    // TODO Sarp: improve this test case
    @Test
    void testCheckWithTransitiveAccess() {
        // Assuming the class has transitive accesses
        condition.check(javaClass, events);

        Collection<ConditionEvent> eventsList = events.getViolating();
        assertThat(eventsList).isNotEmpty();
        assertThat(eventsList.iterator().next()).isInstanceOfAny(SimpleConditionEvent.class);
        assertThat(eventsList.iterator().next().isViolation()).isFalse();
    }

    @Test
    void testCheckWithNoAccesses() {
        // Use a class with no accesses
        javaClass = new ClassFileImporter().importPackages("de.tum.cit.ase.ares.api.architecture.com.example").stream()
                .filter(cls -> cls.getName().equals("de.tum.cit.ase.ares.api.architecture.com.example.EmptyClass"))
                .findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Class not found"));

        condition.check(javaClass, events);

        assertThat(events.getViolating()).isEmpty();
    }

    @Test
    void testTransitiveAccessPath() {
        JavaAccess<?> access = javaClass.getAccessesFromSelf().iterator().next();
        List<JavaAccess<?>> path = condition.new TransitiveAccessPath().findPathTo(access);

        assertThat(path).isNotEmpty();
    }
}
