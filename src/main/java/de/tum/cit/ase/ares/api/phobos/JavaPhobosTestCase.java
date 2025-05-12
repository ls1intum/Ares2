package de.tum.cit.ase.ares.api.phobos;

import de.tum.cit.ase.ares.api.aop.fileSystem.java.JavaFileSystemExtractor;
import de.tum.cit.ase.ares.api.aop.networkConnection.java.JavaNetworkConnectionExtractor;
import de.tum.cit.ase.ares.api.aop.resourceLimits.java.JavaResourceLimitsExtractor;
import de.tum.cit.ase.ares.api.policy.policySubComponents.FilePermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.NetworkPermission;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;

import javax.annotation.Nonnull;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Supplier;

public class JavaPhobosTestCase {


    @Nonnull
    public static String writePhobosSecurityTestCaseFile(@Nonnull List<FilePermission> filePermissions,
                                                         @Nonnull List<NetworkPermission> networkPermissions,
                                                         @Nonnull List<ResourceLimitsPermission> resourceLimitsPermissions) {

        JavaFileSystemExtractor fileSystemExtractor = new JavaFileSystemExtractor(supplier(filePermissions));
        JavaNetworkConnectionExtractor networkConnectionExtractor = new JavaNetworkConnectionExtractor(supplier(networkPermissions));
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


    @SuppressWarnings("unchecked")
    private static <T> Supplier<List<?>> supplier(List<T> list) {
        return () -> (List<?>) list;
    }


    private static Set<String> collectReadOnlyPaths(JavaFileSystemExtractor fs) {
        Set<String> ro = new LinkedHashSet<>(fs.getPermittedFilePaths("read"));
        ro.removeAll(fs.getPermittedFilePaths("overwrite"));
        ro.removeAll(fs.getPermittedFilePaths("delete"));
        return ro;
    }

    private static Set<String> collectWritePaths(JavaFileSystemExtractor fs) {
        Set<String> rw = new LinkedHashSet<>(fs.getPermittedFilePaths("overwrite"));
        rw.addAll(fs.getPermittedFilePaths("delete"));
        return rw;
    }

    private static void appendFilesystemSection(StringBuilder out,
                                                String header,
                                                Set<String> paths) {
        if (paths.isEmpty()) return;
        out.append('[').append(header).append("]\n");
        paths.forEach(p -> out.append(p).append('\n'));
        out.append('\n');
    }


    private static Set<String> collectAllowHosts(JavaNetworkConnectionExtractor net) {
        Set<String> hosts = new LinkedHashSet<>(net.getPermittedNetworkHosts("connect"));
        hosts.addAll(net.getPermittedNetworkHosts("send"));
        return hosts;
    }

    private static Set<Integer> collectAllowPorts(JavaNetworkConnectionExtractor net) {
        Set<Integer> ports = new LinkedHashSet<>(net.getPermittedNetworkPorts("connect"));
        ports.addAll(net.getPermittedNetworkPorts("send"));
        return ports;
    }

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

    private static void appendLimitsSection(StringBuilder out, Map<String, Long> limits) {
        if (limits.isEmpty()) return;
        out.append("[limits]\n");
        limits.forEach((k, v) -> out.append(k).append('=').append(v).append('\n'));
    }
}
