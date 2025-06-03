package de.tum.cit.ase.ares.api.aop.resourceLimits.java;

import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class JavaResourceLimitsExtractor {


    @Nonnull
    private final Supplier<List<?>> resourceAccessSupplier;

    /**
     * Constructs a new JavaResourceLimitsExtractor with the specified resource access supplier.
     *
     * @param resourceAccessSupplier the supplier for the resource accesses permitted as defined in the security policy, must not be null.
     */
    public JavaResourceLimitsExtractor(@Nonnull Supplier<List<?>> resourceAccessSupplier) {
        this.resourceAccessSupplier = resourceAccessSupplier;
    }



    /**
     * Retrieves the tightest timeout limit given by the Java security policy (in case multiple timeouts are configured).
     *
     * @return the minimum timeout value in milliseconds, or a restrictive default if no timeouts are configured.
     */
    @Nonnull
    public Long getTightestTimeout() {
        OptionalLong min =
                ((List<ResourceLimitsPermission>) resourceAccessSupplier.get())
                        .stream()
                        .mapToLong(ResourceLimitsPermission::timeout)
                        .min();
        return min.isPresent() ? min.getAsLong() : ResourceLimitsPermission.createRestrictive().timeout();
    }



    /**
     * Collects the resource limits defined in the Java security policy.
     *
     * @return a map of resource limits where the key is the resource type and the value is the limit.
     */
    @Nonnull
    public Map<String, Long> collectResourceLimits() {
        Map<String, Long> min = new LinkedHashMap<>();

        for (ResourceLimitsPermission p : (List<ResourceLimitsPermission>) resourceAccessSupplier.get()) {
            for (Method m : p.getClass().getMethods()) {
                if (m.getParameterCount() != 0 || m.getReturnType() != long.class) continue;
                String name = m.getName();
                try {
                    long val = (long) m.invoke(p);
                    if (val < 0) continue;
                    min.merge(name, val, Math::min);
                } catch (ReflectiveOperationException e) {
                   continue;
                }
            }
        }
        return min;
    }
}
