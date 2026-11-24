package de.tum.cit.ase.ares.api.architecture.java.packageimport.fixtures;

import java.lang.invoke.MethodHandles;

/**
 * Fixture resolving an enum constant descriptor with a lookup it is handed. The
 * descriptor type lies in {@code java.lang} rather than in
 * {@code java.lang.constant}, so the package prefix alone does not cover it.
 */
public class UsesEnumDescResolution {

	/**
	 * Resolves the described enum constant through the given lookup.
	 *
	 * @param descriptor an enum constant descriptor obtained elsewhere
	 * @param lookup     a lookup obtained elsewhere
	 * @return the resolved enum constant
	 * @throws ReflectiveOperationException if the constant cannot be resolved
	 */
	public Object resolve(Enum.EnumDesc<?> descriptor, MethodHandles.Lookup lookup)
			throws ReflectiveOperationException {
		return descriptor.resolveConstantDesc(lookup);
	}
}
