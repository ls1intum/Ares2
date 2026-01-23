package de.tum.cit.ase.ares.api.aop.java.javaAOPTestCaseToolbox;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

public record JavaAOPAdviceSettingTriple(@Nonnull String dataTyp, @Nonnull String adviceSetting, @Nullable Object value) {
	public JavaAOPAdviceSettingTriple {
		Objects.requireNonNull(dataTyp, "dataTyp must not be null");
		Objects.requireNonNull(adviceSetting, "adviceSetting must not be null");
	}
}
