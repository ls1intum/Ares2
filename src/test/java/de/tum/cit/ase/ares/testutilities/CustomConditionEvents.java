package de.tum.cit.ase.ares.testutilities;

import com.google.common.collect.ImmutableList;
import com.tngtech.archunit.lang.ConditionEvent;
import com.tngtech.archunit.lang.ConditionEvents;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;

/**
 * Custom condition events used in ArchUnit tests
 */
public class CustomConditionEvents implements ConditionEvents {
    private final List<ConditionEvent> violations = new ArrayList<>();
    private Optional<String> informationAboutNumberOfViolations = Optional.empty();

    @Override
    public void add(ConditionEvent event) {
        if (!event.isViolation()) {
            violations.add(event);
        }
    }

    @Override
    public Optional<String> getInformationAboutNumberOfViolations() {
        return informationAboutNumberOfViolations;
    }

    @Override
    public void setInformationAboutNumberOfViolations(String informationAboutNumberOfViolations) {
        this.informationAboutNumberOfViolations = Optional.of(informationAboutNumberOfViolations);
    }

    @Override
    public Collection<ConditionEvent> getViolating() {
        return ImmutableList.copyOf(violations);
    }

    @Override
    public boolean containViolation() {
        return !violations.isEmpty();
    }

    @Override
    public String toString() {
        return getClass().getSimpleName() + "{" + violations + '}';
    }
}
