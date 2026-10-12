package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import de.tum.cit.ase.ares.api.MirrorOutput;
import de.tum.cit.ase.ares.api.localization.Messages;

/**
 * The policy-wide default of {@code @MirrorOutput}: whether a test's console
 * output is also echoed to the real console, and how much output a test may
 * write before it fails.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 * @param theOutputIsMirrored        whether output is echoed; null means off,
 *                                   as without the annotation.
 * @param theMaximumCharacterCountIs the output limit per stream; null means
 *                                   {@link MirrorOutput#DEFAULT_MAX_STD_OUT}.
 */
public record OutputMirroringConfiguration(@Nullable Boolean theOutputIsMirrored,
		@Nullable Long theMaximumCharacterCountIs) {

	/**
	 * Constructs an OutputMirroringConfiguration, rejecting a limit that is not
	 * positive.
	 *
	 * @throws IllegalArgumentException naming the field out of range
	 */
	public OutputMirroringConfiguration {
		if (theMaximumCharacterCountIs != null && theMaximumCharacterCountIs <= 0) {
			throw new IllegalArgumentException(
					Messages.localized("policy.behavior.output.mirroring.limit.range", theMaximumCharacterCountIs));
		}
	}

	/**
	 * Whether output is echoed to the real console, off when not configured.
	 *
	 * @return true when the policy turns mirroring on
	 */
	public boolean mirrored() {
		return Boolean.TRUE.equals(theOutputIsMirrored);
	}

	/**
	 * The output limit per stream, the annotation's default when not configured.
	 *
	 * @return the limit
	 */
	public long maximumCharacterCount() {
		return theMaximumCharacterCountIs == null ? MirrorOutput.DEFAULT_MAX_STD_OUT : theMaximumCharacterCountIs;
	}

	/**
	 * Returns a builder for creating an OutputMirroringConfiguration instance.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @return a new Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for OutputMirroringConfiguration.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 */
	public static final class Builder {

		/** Whether output is echoed, or null when not configured. */
		@Nullable
		private Boolean theOutputIsMirrored;

		/** The output limit, or null when not configured. */
		@Nullable
		private Long theMaximumCharacterCountIs;

		/**
		 * Creates an empty builder; use {@link OutputMirroringConfiguration#builder()}.
		 */
		private Builder() {
		}

		/**
		 * Sets whether output is echoed to the real console.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theOutputIsMirrored the switch, or null when not configured.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theOutputIsMirrored(@Nullable Boolean theOutputIsMirrored) {
			this.theOutputIsMirrored = theOutputIsMirrored;
			return this;
		}

		/**
		 * Sets the output limit per stream.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @param theMaximumCharacterCountIs the limit, or null when not configured.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder theMaximumCharacterCountIs(@Nullable Long theMaximumCharacterCountIs) {
			this.theMaximumCharacterCountIs = theMaximumCharacterCountIs;
			return this;
		}

		/**
		 * Builds a new OutputMirroringConfiguration instance.
		 *
		 * @since 2.1.5
		 * @author Luka Petrovic
		 * @return a new OutputMirroringConfiguration instance.
		 * @throws IllegalArgumentException if the limit is not positive
		 */
		@Nonnull
		public OutputMirroringConfiguration build() {
			return new OutputMirroringConfiguration(theOutputIsMirrored, theMaximumCharacterCountIs);
		}
	}
}
