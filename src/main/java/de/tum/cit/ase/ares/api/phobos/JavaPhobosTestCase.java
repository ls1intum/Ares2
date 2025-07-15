package de.tum.cit.ase.ares.api.phobos;

import de.tum.cit.ase.ares.api.aop.fileSystem.java.JavaFileSystemExtractor;
import de.tum.cit.ase.ares.api.aop.resourceLimits.java.JavaResourceLimitsExtractor;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.phobos.java.JavaPhobosTestCaseSupported;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;
import de.tum.cit.ase.ares.api.aop.networkSystem.java.JavaNetworkSystemExtractor;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

/**
 * Generates a Phobos security test case file based on the provided permissions.
 *
 * <p>Description: This class provides a method to create a Phobos security test case file
 * that includes filesystem, network, and resource limits permissions.
 *
 * <p>Design Rationale: The separation of concerns allows for clear organization of permissions
 * and facilitates the generation of security test cases.
 *
 * @author Ajayvir Singh
 * @since 2.0.1
 */
public class JavaPhobosTestCase extends PhobosTestCase {


    /**
     * The supplier for the resource accesses permitted as defined in the security policy.
     */
    @Nonnull
    private final Supplier<List<?>> resourceAccessSupplier;

    /**
     * Initializes the configuration with the given support type and resource accesses.
     *
     * @param javaTestCaseSupported the type of security test case being supported, must not be null.
     * @param resourceAccessSupplier the resource accesses permitted as defined in the security policy, must not be null.
     */
    public JavaPhobosTestCase(
            @Nonnull JavaPhobosTestCaseSupported javaTestCaseSupported,
            @Nonnull Supplier<List<?>> resourceAccessSupplier
    ) {
        super(
                javaTestCaseSupported,
                new JavaFileSystemExtractor(resourceAccessSupplier),
                new JavaNetworkSystemExtractor(resourceAccessSupplier),
                new JavaResourceLimitsExtractor(resourceAccessSupplier)
        );
        this.resourceAccessSupplier = resourceAccessSupplier;
    }


    @Nonnull
    public static String writePhobosSecurityTestCaseFile(@Nonnull List<FilePermission> filePermissions,
                                                         @Nonnull List<NetworkPermission> networkPermissions,
                                                         @Nonnull List<ResourceLimitsPermission> resourceLimitsPermissions) {

        JavaFileSystemExtractor fileSystemExtractor = new JavaFileSystemExtractor(supplier(filePermissions));
        JavaNetworkSystemExtractor networkConnectionExtractor = new JavaNetworkSystemExtractor(supplier(networkPermissions));
        JavaResourceLimitsExtractor resourceLimitsExtractor = new JavaResourceLimitsExtractor(supplier(resourceLimitsPermissions));

        Set<String> readOnlyPaths = collectReadOnlyPaths(fileSystemExtractor);
        Set<String> writePaths = collectWritePaths(fileSystemExtractor);
        Set<String> allowHosts = collectAllowHosts(networkConnectionExtractor);
        Set<Integer> allowPorts = collectAllowPorts(networkConnectionExtractor);
        Map<String, Long> resourceLimits = resourceLimitsExtractor.collectResourceLimits();

        StringBuilder cfg = new StringBuilder();
        appendFilesystemSection(cfg, "readonly", readOnlyPaths);
        appendFilesystemSection(cfg, "write", writePaths);
        appendNetworkSection(cfg, "network", allowHosts, allowPorts);
        appendLimitsSection(cfg, "limits", resourceLimits);


        return cfg.toString();
    }


    /**
     * Creates a supplier that returns a list of the specified type.
     *
     * @param list the list to be supplied, must not be null.
     * @param <T>  the type of elements in the list.
     * @return a supplier that returns the provided list.
     */
    @SuppressWarnings("unchecked")
    private static <T> Supplier<List<?>> supplier(List<T> list) {
        return () -> (List<?>) list;
    }


    /**
     * Starts the builder for the Java phobos test case.
     */
    public static JavaPhobosTestCase.Builder builder() {
        return new JavaPhobosTestCase.Builder();
    }



    /**
     * Collects read-only file paths from the provided JavaFileSystemExtractor.
     *
     * @param fs the JavaFileSystemExtractor to extract paths from, must not be null.
     * @return a set of read-only file paths.
     */
    private static Set<String> collectReadOnlyPaths(JavaFileSystemExtractor fs) {
        Set<String> ro = new LinkedHashSet<>(fs.getPermittedFilePaths("read"));
        ro.addAll(fs.getPermittedFilePaths("execute"));
        ro.removeAll(fs.getPermittedFilePaths("overwrite"));
        ro.removeAll(fs.getPermittedFilePaths("delete"));
        return ro;
    }

    /**
     * Collects write file paths from the provided JavaFileSystemExtractor.
     *
     * @param fs the JavaFileSystemExtractor to extract paths from, must not be null.
     * @return a set of write file paths.
     */
    private static Set<String> collectWritePaths(JavaFileSystemExtractor fs) {
        Set<String> rw = new LinkedHashSet<>(fs.getPermittedFilePaths("overwrite"));
        rw.addAll(fs.getPermittedFilePaths("delete"));
        return rw;
    }

    /**
     * Appends and formats a section for filesystem paths to the provided StringBuilder.
     * e.g.
     * [readonly]
     * /path/to/file1
     * /path/to/file2
     *
     * @param out    the StringBuilder to append to, must not be null.
     * @param header the header for the section, must not be null.
     * @param paths  the set of paths to append, must not be null.
     */
    private static void appendFilesystemSection(StringBuilder out,
                                                String header,
                                                Set<String> paths) {
        if (paths.isEmpty()) {
            return;
        }
        out.append('[').append(header).append("]\n");

        List<String> sortedPaths = paths.stream()
                .sorted()
                .toList();
        sortedPaths.forEach(p -> out.append(p).append('\n'));
        out.append('\n');
    }


    /**
     * // TODO: hosts and ports should have the same length -> first host = first port, second host = second port, etc.
     * HOSTS: ["example1.com", "example2.com"]
     * PORTS: [80, 444]
     */
    /**
     * Collects allowed network hosts from the provided JavaNetworkSystemExtractor.
     *
     * @param net the JavaNetworkSystemExtractor to extract hosts from, must not be null.
     * @return a set of allowed network hosts.
     */
    private static Set<String> collectAllowHosts(JavaNetworkSystemExtractor net) {
        Set<String> hosts = new LinkedHashSet<>(net.getPermittedNetworkHosts("connect"));
        hosts.addAll(net.getPermittedNetworkHosts("send"));
        return hosts;
    }

    @Nonnull
    public Supplier<List<?>> getResourceAccessSupplier() {
        return resourceAccessSupplier;
    }

    /**
     * Collects allowed network ports from the provided JavaNetworkSystemExtractor.
     *
     * @param net the JavaNetworkSystemExtractor to extract ports from, must not be null.
     * @return a set of allowed network ports.
     */
    private static Set<Integer> collectAllowPorts(JavaNetworkSystemExtractor net) {
        Set<Integer> ports = new LinkedHashSet<>(net.getPermittedNetworkPorts("connect"));
        ports.addAll(net.getPermittedNetworkPorts("send"));
        return ports;
    }

    /**
     * Appends and formats a section for network permissions to the provided StringBuilder.
     * e.g.
     * [network]
     * deny *
     * allow host1
     *
     * @param out     the StringBuilder to append to, must not be null.
     * @param header  the header for the section, must not be null.
     * @param hosts   the set of allowed network hosts, must not be null.
     * @param ports   the set of allowed network ports, must not be null.
     */
    private static void appendNetworkSection(StringBuilder out,
                                             String header, Set<String> hosts,
                                             Set<Integer> ports) {


        if (hosts.isEmpty()) {
            return;
        }

        out.append('[').append(header).append("]\n");
        out.append("deny *\n");


        List<String> outputHosts = hosts.stream()
                .sorted()
                .toList();

        List<Integer> outputPorts = ports.stream()
                .sorted()
                .toList();

        if (ports.isEmpty()) {
            outputHosts.forEach(h -> out.append("allow ").append(h).append('\n'));
        } else {

            for (String h : outputHosts) {
                for (Integer p : outputPorts) {
                    if (p == 0) {
                        out.append("allow ").append(h).append('\n');
                    } else {
                        out.append("allow ").append(h).append(':').append(p).append('\n');
                    }
                }
            }
        }
        out.append('\n');
    }

    /**
     * Appends and formats a section for resource limits to the provided StringBuilder.
     * e.g.
     * [limits]
     * memory=1024
     * cpu=2
     *
     * @param out    the StringBuilder to append to, must not be null.
     * @param header header for the section, must not be null.
     * @param limits the map of resource limits, must not be null.
     */
    private static void appendLimitsSection(StringBuilder out, String header, Map<String, Long> limits) {
        if (limits.isEmpty()) {
            return;
        }
        out.append('[').append(header).append("]\n");
        limits.forEach((k, v) -> out.append(k).append('=').append(v).append('\n'));
        out.append('\n');

    }





    /**
     * Generates the content for the phobos security test case.
     * <p>This method provides an empty implementation for now but will be overridden in future
     * configurations to generate phobos configuration files based on the provided security policies.</p>
     *
     * @return a string representing the content of the configuration.
     */
    @Override
    public String writePhobosTestCase() {
        return "";
    }

    public static class Builder {

        private JavaPhobosTestCaseSupported javaPhobosTestCaseSupported;
        private Supplier<List<?>> resourceAccessSupplier;

        public JavaPhobosTestCase.Builder javaPhobosTestCaseSupported(@Nonnull JavaPhobosTestCaseSupported javaPhobosTestCaseSupported) {

            if (javaPhobosTestCaseSupported == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "javaPhobosTestCaseSupported"));
            }
            this.javaPhobosTestCaseSupported = javaPhobosTestCaseSupported;
            return this;
        }

        public JavaPhobosTestCase.Builder resourceAccessSupplier(Supplier<List<?>> resourceAccessSupplier) {
            if (resourceAccessSupplier == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "resourceAccessSupplier"));
            }
            this.resourceAccessSupplier = resourceAccessSupplier;
            return this;
        }

        public JavaPhobosTestCase build() {
            if (javaPhobosTestCaseSupported == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "javaPhobosTestCaseSupported"));
            }
            if (resourceAccessSupplier == null) {
                throw new SecurityException(Messages.localized("security.common.not.null", "resourceAccessSupplier"));
            }
            return new JavaPhobosTestCase(javaPhobosTestCaseSupported, resourceAccessSupplier);
        }
    }
}
