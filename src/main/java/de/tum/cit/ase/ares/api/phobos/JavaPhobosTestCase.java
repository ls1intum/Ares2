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
import java.util.*;
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
        Set<String> allowHostsAndPorts = collectAllowHostsAndPorts(networkConnectionExtractor);
        Map<String, Long> resourceLimits = resourceLimitsExtractor.collectResourceLimits();

        StringBuilder cfg = new StringBuilder();
        appendFilesystemSection(cfg, "readonly", readOnlyPaths);
        appendFilesystemSection(cfg, "write", writePaths);
        appendNetworkSection(cfg, "network", allowHostsAndPorts);
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



    @Nonnull
    public Supplier<List<?>> getResourceAccessSupplier() {
        return resourceAccessSupplier;
    }


    /**
     * Collects allowed network hosts from the provided JavaNetworkSystemExtractor.
     *
     * <p>The extractor must deliver <em>parallel</em> host- and port-lists:<br>
     * first host = first port<br>
     * Any other configuration is considered invalid and triggers an
     * {@link IllegalStateException}.</p>
     *
     * <p>The resulting set contains strings of the form
     * {@code "host"} (when the mapped port is {@code 0}) or
     * {@code "host:port"}; order is preserved so that later code that relies
     * on declaration order (e.g. an <code>LD_PRELOAD</code> helper) still
     * works.</p>
     *
     * @param net the JavaNetworkSystemExtractor, must not be {@code null}
     * @return an ordered set of <code>host[:port]</code> strings
     * @throws IllegalStateException if the host and port lists differ in length
     * @throws NullPointerException  if {@code net} is {@code null}
     */
    private static Set<String> collectAllowHostsAndPorts(JavaNetworkSystemExtractor net) {
        Objects.requireNonNull(net, "extractor must not be null");

        Set<String> allowed = new LinkedHashSet<>();

        for (String verb : List.of("connect", "send")) {          // add "receive" if/when needed
            List<String> hosts = net.getPermittedNetworkHosts(verb);
            List<Integer> ports = net.getPermittedNetworkPorts(verb);

            if (hosts.size() != ports.size()) {
                throw new IllegalStateException(String.format(
                        "Mismatching host/port lists for '%s' (hosts=%d, ports=%d)",
                        verb, hosts.size(), ports.size()));
            }

            for (int i = 0; i < hosts.size(); i++) {
                String host = hosts.get(i);
                int port    = ports.get(i);
                allowed.add(port == 0 ? host : host + ':' + port);
            }
        }

        return allowed;
    }

    /**
     * Appends a formatted “[network…]” policy section to {@code out}.
     * The {@code hostsAndPorts} set must already contain fully formatted
     * {@code host[:port]} strings (exactly what {@link #collectAllowHostsAndPorts} returns).
     * Example output: <pre>
     * [network]
     * deny *
     * allow example1.com
     * allow example2.com:444
     * </pre>
     *
     * @param out            StringBuilder to append to, must not be {@code null}
     * @param header         section header (without brackets), e.g. {@code "network"}, must not be {@code null}
     * @param hostsAndPorts  ordered set of {@code host[:port]} strings, must not be {@code null}
     */
    private static void appendNetworkSection(StringBuilder out,
                                             String header,
                                             Set<String> hostsAndPorts) {

        Objects.requireNonNull(out,           "out must not be null");
        Objects.requireNonNull(header,        "header must not be null");
        Objects.requireNonNull(hostsAndPorts, "hostsAndPorts must not be null");

        if (hostsAndPorts.isEmpty()) {
            return;
        }

        out.append('[').append(header).append("]\n")
                .append("deny *\n");

        hostsAndPorts.stream()
                .sorted()
                .forEach(hp -> out.append("allow ").append(hp).append('\n'));

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
