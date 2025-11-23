package de.tum.cit.ase.ares.api.aop.resourceLimits.java;

import de.tum.cit.ase.ares.api.aop.resourceLimits.ResourceLimitsExtractor;
import de.tum.cit.ase.ares.api.localization.Messages;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceLimitsPermission;

import javax.annotation.Nonnull;
import java.lang.reflect.Method;
import java.util.*;
import java.util.function.Supplier;

public class JavaResourceLimitsExtractor implements ResourceLimitsExtractor {


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
        List<?> rawList = resourceAccessSupplier.get();
        if (rawList == null) {
            throw new SecurityException(
                    Messages.localized("security.resource.limits.permission.null.list")
            );
        }

        if (rawList.isEmpty()) {
            return ResourceLimitsPermission.createRestrictive().timeout();
        }

        for (Object item : rawList) {
            if (item == null) {
                throw new SecurityException(
                        Messages.localized("security.resource.limits.permission.null.entry")
                );
            }

            if (!(item instanceof ResourceLimitsPermission)) {
                throw new SecurityException(
                        Messages.localized(
                                "security.resource.limits.permission.invalid.type",
                                item.getClass().getName()
                        )
                );
            }
        }


        OptionalLong min = rawList.stream()
                .map(ResourceLimitsPermission.class::cast).mapToLong(ResourceLimitsPermission::timeout)
                .min();
        return min.getAsLong();
    }


    /**
     * Collects the resource limits defined in the Java security policy.
     *
     * @return a map of resource limits where the key is the resource type and the value is the limit.
     */
    @Nonnull
    public Map<String, Long> collectResourceLimits() {
        Map<String, Long> min = new LinkedHashMap<>();
        List<?> rawList = resourceAccessSupplier.get();
        for (Object obj : rawList) {
            if (!(obj instanceof ResourceLimitsPermission p)) {
                continue; // Skip non-ResourceLimitsPermission objects
            }
            for (Method m : p.getClass().getMethods()) {
                // Only process public methods with no parameters that return long
                if (m.getParameterCount() != 0 ||
                        m.getReturnType() != long.class ||
                        !java.lang.reflect.Modifier.isPublic(m.getModifiers())) {
                    continue;
                }
                String name = m.getName();
                try {
                    long val = (long) m.invoke(p);
                    if (val < 0) continue;
                    min.merge(name, val, Math::min);
                } catch (ReflectiveOperationException e) {
                    throw new SecurityException(
                            Messages.localized(
                                    "security.resource.limits.permission.invocation.error",
                                    name,
                                    String.valueOf(e.getMessage())
                            ),
                            e);
                }
            }
        }
        return min;
    }
}
