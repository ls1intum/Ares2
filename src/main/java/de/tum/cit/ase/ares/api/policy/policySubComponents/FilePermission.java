package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Allowed file operations.
 *
 * <p>Description: Specifies whether reading, overwriting, executing, and deleting files is permitted on a given path.
 *
 * <p>Design Rationale: Explicitly defining file operation permissions helps ensure secure code execution.
 *
 * @since 2.0.0
 * @author Markus Paulsen
 * @since 2.0.0
 * @param readAllFiles whether reading all files is permitted.
 * @param overwriteAllFiles whether overwriting all files is permitted.
 * @param executeAllFiles whether executing all files is permitted.
 * @param deleteAllFiles whether deleting all files is permitted.
 * @param onThisPathAndAllPathsBelow the path where these permissions apply; must not be null.
 */
public record FilePermission(
        @Nonnull String onThisPathAndAllPathsBelow,
        boolean readAllFiles,
        boolean overwriteAllFiles,
        boolean executeAllFiles,
        boolean deleteAllFiles
) {

    /**
     * Constructs a FilePermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     */
    public FilePermission {
        Objects.requireNonNull(onThisPathAndAllPathsBelow, "onThisPathAndAllPathsBelow must not be null");
        if (onThisPathAndAllPathsBelow.isBlank()) {
            throw new IllegalArgumentException("onThisPathAndAllPathsBelow must not be blank");
        }
    }

    /**
     * Creates a restrictive file permission with all operations denied.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @param path the path where the restrictions apply.
     * @return a new FilePermission instance with all operations denied.
     */
    @Nonnull
    public static FilePermission createRestrictive(String path) {
        return builder().onThisPathAndAllPathsBelow(Objects.requireNonNull(path, "path must not be null")).readAllFiles(false).overwriteAllFiles(false).executeAllFiles(false).deleteAllFiles(false).build();
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
     *
     * <p>Description: Provides a fluent API to construct a FilePermission instance.
     *
     * <p>Design Rationale: The builder pattern facilitates the step-by-step construction of file permissions.
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
         * @param onThisPathAndAllPathsBelow the file path.
         * @return the updated Builder.
         */
        @Nonnull
        public Builder onThisPathAndAllPathsBelow(@Nonnull String onThisPathAndAllPathsBelow) {
            this.onThisPathAndAllPathsBelow = Objects.requireNonNull(onThisPathAndAllPathsBelow, "onThisPathAndAllPathsBelow must not be null");
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
         * @return a new FilePermission instance.
         */
        @Nonnull
        public FilePermission build() {
            return new FilePermission(Objects.requireNonNull(onThisPathAndAllPathsBelow, "path must not be null"), readAllFiles, overwriteAllFiles, executeAllFiles, deleteAllFiles);
        }
    }
}
