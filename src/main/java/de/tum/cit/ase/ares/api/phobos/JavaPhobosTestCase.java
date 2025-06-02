package de.tum.cit.ase.ares.api.phobos;

import de.tum.cit.ase.ares.api.aop.fileSystem.java.JavaFileSystemExtractor;
import de.tum.cit.ase.ares.api.aop.resourceLimits.java.JavaResourceLimitsExtractor;
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
 * @since 2.0.0
 * @author Ajayvir Singh
 */
public class JavaPhobosTestCase {


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
        appendNetworkSection(cfg, allowHosts, allowPorts);
        appendLimitsSection(cfg, resourceLimits);

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
     * Collects read-only file paths from the provided JavaFileSystemExtractor.
     *
     * @param fs the JavaFileSystemExtractor to extract paths from, must not be null.
     * @return a set of read-only file paths.
     */
    private static Set<String> collectReadOnlyPaths(JavaFileSystemExtractor fs) {
        Set<String> ro = new LinkedHashSet<>(fs.getPermittedFilePaths("read"));
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
        if (paths.isEmpty()) return;
        out.append('[').append(header).append("]\n");
        paths.forEach(p -> out.append(p).append('\n'));
        out.append('\n');
    }


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
     * @param out the StringBuilder to append to, must not be null.
     * @param hosts the set of allowed network hosts, must not be null.
     * @param ports the set of allowed network ports, must not be null.
     */
    private static void appendNetworkSection(StringBuilder out,
                                             Set<String> hosts,
                                             Set<Integer> ports) {
        out.append("[network]\n");
        out.append("deny *\n");

        if (hosts.isEmpty()) {
            out.append('\n');
            return;
        }

        if (ports.isEmpty()) {
            hosts.forEach(h -> out.append("allow ").append(h).append('\n'));
        } else {
            for (String h : hosts) {
                for (Integer p : ports) {
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
     * @param limits the map of resource limits, must not be null.
     */
    private static void appendLimitsSection(StringBuilder out, Map<String, Long> limits) {
        if (limits.isEmpty()) return;
        out.append("[limits]\n");
        limits.forEach((k, v) -> out.append(k).append('=').append(v).append('\n'));
    }
}
