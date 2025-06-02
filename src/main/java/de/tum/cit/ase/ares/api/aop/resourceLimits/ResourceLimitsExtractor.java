package de.tum.cit.ase.ares.api.aop.resourceLimits;

import java.util.Map;

public interface ResourceLimitsExtractor {

    /**
     * Retrieves the tightest timeout limit given by the security policy (in case multiple timeouts are configured).
     *
     * @return the minimum timeout value in milliseconds, or a restrictive default if no timeouts are configured.
     */
    Long getTightestTimeout();


    /**
     * Collects the resource limits defined in the security policy.
     *
     * @return a map of resource limits where the key is the resource type and the value is the limit.
     */
    Map<String, Long> collectResourceLimits();
}
