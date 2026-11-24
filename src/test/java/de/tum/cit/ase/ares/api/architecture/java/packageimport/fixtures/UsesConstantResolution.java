package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.lang.constant.ClassDesc;
import java.lang.invoke.MethodHandles;

/**
 * Fixture resolving a constant descriptor with a lookup it is handed, which
 * loads a class without calling {@code java.lang.invoke} or
 * {@code java.lang.reflect} itself.
 */
public class UsesConstantResolution {

	/**
	 * Resolves a class through the given lookup.
	 *
	 * @param lookup a lookup obtained elsewhere
	 * @return the resolved class, typed as the descriptor API returns it on Java 17
	 * @throws ReflectiveOperationException if the class cannot be resolved
	 */
	public Object resolve(MethodHandles.Lookup lookup) throws ReflectiveOperationException {
		return ClassDesc.of("java.lang.String").resolveConstantDesc(lookup);
	}
}
