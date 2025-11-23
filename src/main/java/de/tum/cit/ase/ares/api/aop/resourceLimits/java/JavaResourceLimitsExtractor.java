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
        this.resourceAccessSupplier = Objects.requireNonNull(resourceAccessSupplier, "resourceAccessSupplier must not be null");
    }


    /**
     * Retrieves the tightest timeout limit given by the Java security policy (in case multiple timeouts are configured).
     *
     * @return the minimum timeout value in milliseconds, or a restrictive default if no timeouts are configured.
     */
    @Override
    @Nonnull
    public Long getTightestTimeout() {
        List<ResourceLimitsPermission> permissions = validatePermissions(resourceAccessSupplier.get());

        if (permissions.isEmpty()) {
            return ResourceLimitsPermission.createRestrictive().timeout();
        }

        OptionalLong min = permissions.stream().mapToLong(ResourceLimitsPermission::timeout)
                .filter(timeoutValue -> timeoutValue >= 0)
                .min();
        return min.orElse(ResourceLimitsPermission.createRestrictive().timeout());
    }


    /**
     * Collects the resource limits defined in the Java security policy.
     *
     * @return a map of resource limits where the key is the resource type and the value is the limit.
     */
    @Override
    @Nonnull
    public Map<String, Long> collectResourceLimits() {
        Map<String, Long> min = new LinkedHashMap<>();
        List<ResourceLimitsPermission> permissions = validatePermissions(resourceAccessSupplier.get());
        for (ResourceLimitsPermission p : permissions) {
            for (Method m : p.getClass().getMethods()) {
                if (m.getParameterCount() != 0 ||
                        m.getReturnType() != long.class ||
                        !java.lang.reflect.Modifier.isPublic(m.getModifiers()) ||
                        java.lang.reflect.Modifier.isStatic(m.getModifiers())) {
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

    private List<ResourceLimitsPermission> validatePermissions(List<?> rawList) {
            List<ResourceLimitsPermission> validatedList = new ArrayList<>();
            for (Object item : rawList) {
                    if (item instanceof ResourceLimitsPermission permission) {
                            validatedList.add(permission);
                        }
                }
            return validatedList;
        }
}
