package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;
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
@Nonnull
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
        Objects.requireNonNull(onThisPathAndAllPathsBelow, "Path must not be null");
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
    public static FilePermission createRestrictive(String path) {
        return new FilePermission(path, false, false, false, false);
    }

    /**
     * Returns a builder for creating a FilePermission instance.
     *
     * @since 2.0.0
     * @author Markus Paulsen
     * @return a new FilePermission.Builder instance.
     */
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

        private boolean read;
        private boolean overwrite;
        private boolean execute;
        private boolean delete;
        private String path;

        /**
         * Sets whether reading files is permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param read whether reading is permitted.
         * @return the updated Builder.
         */
        public Builder read(boolean read) {
            this.read = read;
            return this;
        }

        /**
         * Sets whether overwriting files is permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param overwrite whether overwriting is permitted.
         * @return the updated Builder.
         */
        public Builder overwrite(boolean overwrite) {
            this.overwrite = overwrite;
            return this;
        }

        /**
         * Sets whether executing files is permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param execute whether executing is permitted.
         * @return the updated Builder.
         */
        public Builder execute(boolean execute) {
            this.execute = execute;
            return this;
        }

        /**
         * Sets whether deleting files is permitted.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param delete whether deleting is permitted.
         * @return the updated Builder.
         */
        public Builder delete(boolean delete) {
            this.delete = delete;
            return this;
        }

        /**
         * Sets the path where these permissions apply.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @param path the file path.
         * @return the updated Builder.
         */
        public Builder path(String path) {
            this.path = path;
            return this;
        }

        /**
         * Builds a new FilePermission instance.
         *
         * @since 2.0.0
         * @author Markus Paulsen
         * @return a new FilePermission instance.
         */
        public FilePermission build() {
            return new FilePermission(path, read, overwrite, execute, delete);
        }
    }
}
