package de.tum.cit.ase.ares.api.policy.policySubComponents;

import java.util.Objects;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

/**
 * Allowed file operations.
 * <p>
 * Description: Specifies whether reading, overwriting, creating, executing and
 * deleting files is permitted, for one path and everything beneath it. Each
 * operation is granted separately, so a permission that grants none of them
 * denies the path outright.
 * <p>
 * Design Rationale: Explicitly defining file operation permissions helps ensure
 * secure code execution. The path is validated on construction: it may name a
 * location, use one of the recognised placeholders such as
 * {@code ${PROJECT_ROOT}}, or be {@code *}, but it may never walk upwards
 * through {@code ..}, which would otherwise let a policy reach outside the
 * location it appears to describe.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @param onThisPathAndAllPathsBelow the path where these permissions apply,
 *                                   together with everything beneath it, or
 *                                   {@code *} for every path; must not be null.
 * @param readAllFiles               whether reading is permitted there.
 * @param overwriteAllFiles          whether overwriting is permitted there.
 * @param createAllFiles             whether creating is permitted there.
 * @param executeAllFiles            whether executing is permitted there.
 * @param deleteAllFiles             whether deleting is permitted there.
 */
public record FilePermission(@Nonnull String onThisPathAndAllPathsBelow, boolean readAllFiles,
		boolean overwriteAllFiles, boolean createAllFiles, boolean executeAllFiles, boolean deleteAllFiles) {

	/**
	 * Constructs a FilePermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @throws NullPointerException     if the path is null.
	 * @throws IllegalArgumentException if the path is blank, contains a wildcard
	 *                                  other than a sole {@code *}, uses an
	 *                                  unrecognised {@code ${...}} placeholder, or
	 *                                  walks upwards through {@code ..}.
	 */
	public FilePermission {
		Objects.requireNonNull(onThisPathAndAllPathsBelow, "onThisPathAndAllPathsBelow must not be null");
		PolicyValueValidator.requireMatch("onThisPathAndAllPathsBelow", onThisPathAndAllPathsBelow,
				PolicyValueValidator.FILE_PATH_PATTERN);
	}

	/**
	 * Creates a restrictive file permission with all operations denied.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @param path the path where the restrictions apply; must not be null.
	 * @return a new FilePermission instance with all operations denied.
	 * @throws NullPointerException     if the path is null.
	 * @throws IllegalArgumentException if the path is not a valid policy path.
	 */
	@Nonnull
	public static FilePermission createRestrictive(String path) {
		return builder().onThisPathAndAllPathsBelow(Objects.requireNonNull(path, "path must not be null"))
				.readAllFiles(false).overwriteAllFiles(false).createAllFiles(false).executeAllFiles(false)
				.deleteAllFiles(false).build();
	}

	/**
	 * Returns a builder for creating a FilePermission instance.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 * @return a new FilePermission.Builder instance.
	 */
	@Nonnull
	public static Builder builder() {
		return new Builder();
	}

	/**
	 * Builder for FilePermission.
	 * <p>
	 * Description: Provides a fluent API to construct a FilePermission instance.
	 * <p>
	 * Design Rationale: The builder pattern facilitates the step-by-step
	 * construction of file permissions.
	 *
	 * @since 2.0.0
	 * @author Markus Paulsen
	 */
	public static class Builder {

		/**
		 * The path where these permissions apply.
		 */
		@Nullable
		private String onThisPathAndAllPathsBelow;
		/**
		 * Whether reading files is permitted.
		 */
		private boolean readAllFiles;
		/**
		 * Whether overwriting files is permitted.
		 */
		private boolean overwriteAllFiles;
		/**
		 * Whether creating files is permitted.
		 */
		private boolean createAllFiles;
		/**
		 * Whether executing files is permitted.
		 */
		private boolean executeAllFiles;
		/**
		 * Whether deleting files is permitted.
		 */
		private boolean deleteAllFiles;

		/**
		 * Sets the path where these permissions apply.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param onThisPathAndAllPathsBelow the file path; must not be null, and is
		 *                                   validated for shape by {@link #build()}.
		 * @return the updated Builder.
		 * @throws NullPointerException if the path is null.
		 */
		@Nonnull
		public Builder onThisPathAndAllPathsBelow(@Nonnull String onThisPathAndAllPathsBelow) {
			this.onThisPathAndAllPathsBelow = Objects.requireNonNull(onThisPathAndAllPathsBelow,
					"onThisPathAndAllPathsBelow must not be null");
			return this;
		}

		/**
		 * Sets whether reading files is permitted.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param readAllFiles whether reading is permitted.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder readAllFiles(boolean readAllFiles) {
			this.readAllFiles = readAllFiles;
			return this;
		}

		/**
		 * Sets whether overwriting files is permitted.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param overwriteAllFiles whether overwriting is permitted.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder overwriteAllFiles(boolean overwriteAllFiles) {
			this.overwriteAllFiles = overwriteAllFiles;
			return this;
		}

		/**
		 * Sets whether creating files is permitted.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param createAllFiles whether creating is permitted.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder createAllFiles(boolean createAllFiles) {
			this.createAllFiles = createAllFiles;
			return this;
		}

		/**
		 * Sets whether executing files is permitted.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param executeAllFiles whether executing is permitted.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder executeAllFiles(boolean executeAllFiles) {
			this.executeAllFiles = executeAllFiles;
			return this;
		}

		/**
		 * Sets whether deleting files is permitted.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @param deleteAllFiles whether deleting is permitted.
		 * @return the updated Builder.
		 */
		@Nonnull
		public Builder deleteAllFiles(boolean deleteAllFiles) {
			this.deleteAllFiles = deleteAllFiles;
			return this;
		}

		/**
		 * Builds a new FilePermission instance.
		 *
		 * @since 2.0.0
		 * @author Markus Paulsen
		 * @return a new FilePermission instance. Every operation not set defaults to
		 *         denied.
		 * @throws NullPointerException     if no path was set.
		 * @throws IllegalArgumentException if the path is not a valid policy path.
		 */
		@Nonnull
		public FilePermission build() {
			return new FilePermission(
					Objects.requireNonNull(onThisPathAndAllPathsBelow, "onThisPathAndAllPathsBelow must not be null"),
					readAllFiles, overwriteAllFiles, createAllFiles, executeAllFiles, deleteAllFiles);
		}
	}
}
