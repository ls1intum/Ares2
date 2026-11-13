package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Optional;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The policy-wide default of {@code @MirrorOutput} and {@code @WithIOManager}:
 * which I/O manager a test uses, whether its console output is also echoed, and
 * how much output it may write before it fails. Every field is optional.
 *
 * @since 2.3.0
 * @author Luka Petrovic
 * @param theOutputIsMirrored                  whether output is echoed; null
 *                                             means off.
 * @param theMaximumNumberOfOutputCharactersIs the output limit per stream; null
 *                                             means the annotation's default.
 * @param theInputAndOutputAreManagedBy        the manager; null means
 *                                             {@link InputOutputManagerKind#IO_TESTER}.
 * @param theCustomInputAndOutputManagerIs     the custom manager's class name;
 *                                             only with {@code CUSTOM}.
 */
public record OutputConfiguration(@Nullable Boolean theOutputIsMirrored,
		@Nullable Long theMaximumNumberOfOutputCharactersIs,
		@Nullable InputOutputManagerKind theInputAndOutputAreManagedBy,
		@Nullable String theCustomInputAndOutputManagerIs) {

	/**
	 * Constructs an OutputConfiguration, refusing a limit that is not positive, a
	 * custom manager without a class name or a class name without a custom manager,
	 * and output settings beside {@code NONE}, which would do nothing.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @throws IllegalArgumentException naming the offending field.
	 */
	public OutputConfiguration {
		if (theMaximumNumberOfOutputCharactersIs != null && theMaximumNumberOfOutputCharactersIs <= 0) {
			throw new IllegalArgumentException(Messages.localized("policy.behavior.output.mirroring.limit.range",
					theMaximumNumberOfOutputCharactersIs));
		}
		boolean custom = theInputAndOutputAreManagedBy == InputOutputManagerKind.CUSTOM;
		boolean named = theCustomInputAndOutputManagerIs != null && !theCustomInputAndOutputManagerIs.isBlank();
		if (custom && !named) {
			throw new IllegalArgumentException(Messages.localized("policy.behavior.io.manager.custom.missing"));
		}
		if (!custom && theCustomInputAndOutputManagerIs != null) {
			throw new IllegalArgumentException(Messages.localized("policy.behavior.io.manager.custom.unexpected",
					theCustomInputAndOutputManagerIs));
		}
		if (theInputAndOutputAreManagedBy == InputOutputManagerKind.NONE
				&& (theOutputIsMirrored != null || theMaximumNumberOfOutputCharactersIs != null)) {
			throw new IllegalArgumentException(Messages.localized("policy.behavior.io.manager.none.output"));
		}
	}

	/**
	 * Returns whether output is echoed, false when not configured.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return whether output is echoed.
	 */
	public boolean mirrored() {
		return Boolean.TRUE.equals(theOutputIsMirrored);
	}

	/**
	 * Returns the output limit, the annotation's default when not configured.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return the output limit per stream.
	 */
	public long maximumCharacterCount() {
		return theMaximumNumberOfOutputCharactersIs == null ? MirrorOutput.DEFAULT_MAX_STD_OUT
				: theMaximumNumberOfOutputCharactersIs;
	}

	/**
	 * Returns the manager, {@code IO_TESTER} when not configured.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return the manager kind.
	 */
	@Nonnull
	public InputOutputManagerKind managerKind() {
		return theInputAndOutputAreManagedBy == null ? InputOutputManagerKind.IO_TESTER : theInputAndOutputAreManagedBy;
	}

	/**
	 * Returns the custom manager's class name, present only with {@code CUSTOM}.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return the class name; empty unless the manager is custom.
	 */
	@Nonnull
	public Optional<String> customManagerClassName() {
		return Optional.ofNullable(theCustomInputAndOutputManagerIs).map(String::strip);
	}

	/**
	 * Returns a builder for creating an OutputConfiguration instance.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 * @return a new Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for OutputConfiguration.
	 *
	 * @since 2.3.0
	 * @author Luka Petrovic
	 */
	public static final class Builder {

		/** Whether output is echoed, or null for not configured. */
		@Nullable
		private Boolean theOutputIsMirrored;

		/** The output limit, or null for not configured. */
		@Nullable
		private Long theMaximumNumberOfOutputCharactersIs;

		/** The manager, or null for not configured. */
		@Nullable
		private InputOutputManagerKind theInputAndOutputAreManagedBy;

		/** The custom manager's class name, or null for none. */
		@Nullable
		private String theCustomInputAndOutputManagerIs;

		/** Use {@link OutputConfiguration#builder()}. */
		private Builder() {
		}

		/**
		 * Sets whether output is echoed.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param theOutputIsMirrored whether output is echoed; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theOutputIsMirrored(@Nullable Boolean theOutputIsMirrored) {
			this.theOutputIsMirrored = theOutputIsMirrored;
			return this;
		}

		/**
		 * Sets the output limit.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param theMaximumNumberOfOutputCharactersIs the limit; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theMaximumNumberOfOutputCharactersIs(@Nullable Long theMaximumNumberOfOutputCharactersIs) {
			this.theMaximumNumberOfOutputCharactersIs = theMaximumNumberOfOutputCharactersIs;
			return this;
		}

		/**
		 * Sets the manager.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param theInputAndOutputAreManagedBy the manager; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theInputAndOutputAreManagedBy(@Nullable InputOutputManagerKind theInputAndOutputAreManagedBy) {
			this.theInputAndOutputAreManagedBy = theInputAndOutputAreManagedBy;
			return this;
		}

		/**
		 * Sets the custom manager's class name.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @param theCustomInputAndOutputManagerIs the class name; may be null.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theCustomInputAndOutputManagerIs(@Nullable String theCustomInputAndOutputManagerIs) {
			this.theCustomInputAndOutputManagerIs = theCustomInputAndOutputManagerIs;
			return this;
		}

		/**
		 * Builds a new OutputConfiguration instance.
		 *
		 * @since 2.3.0
		 * @author Luka Petrovic
		 * @return a new OutputConfiguration instance.
		 * @throws IllegalArgumentException when the values do not fit together.
		 */
		@Nonnull
		public OutputConfiguration build() {
			return new OutputConfiguration(theOutputIsMirrored, theMaximumNumberOfOutputCharactersIs,
					theInputAndOutputAreManagedBy, theCustomInputAndOutputManagerIs);
		}
	}
}
