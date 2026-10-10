package de.tum.cit.ase.ares.api.policy.policySubComponents;

import javax.annotation.Nonnull;

/**
 * Enumerates the named policy presets a policy YAML file may reference.
 * <p>
 * Description: Each constant carries the file name of its own bundled preset
 * resource under {@code policy/presets/} in the compiled classpath, a plain
 * {@code SecurityPolicy} YAML file resolved and merged with the referencing
 * policy by the reader before anything else sees it.
 * <p>
 * Design Rationale: The resource file name is a constructor argument rather
 * than derived from the constant name, so adding a preset stays a documented
 * two-step process (add the YAML resource, add one constant naming it) with no
 * separate naming-convention transformation to get right.
 *
 * @since 2.1.5
 * @author Luka Petrovic
 */
public enum SecurityPolicyPreset {

	/**
	 * A throwaway preset proving the reference/composition mechanism round-trips.
	 * Not a real preset for instructors to choose — replaced or joined by the
	 * concrete named presets a later suggestion adds.
	 */
	SMOKE_TEST("smoke-test.yaml");

	/**
	 * The file name of this preset's bundled resource under
	 * {@code policy/presets/}.
	 */
	@Nonnull
	private final String resourceFileName;

	/**
	 * Creates a preset naming its bundled resource. The parameter carries no
	 * annotation on purpose: javac records it against a constructor that also has
	 * the hidden enum name and ordinal parameters, and JDK 21.0.12 then rejects the
	 * compiled class as a bad class file.
	 *
	 * @param resourceFileName the resource file name; must not be null.
	 */
	SecurityPolicyPreset(String resourceFileName) {
		this.resourceFileName = resourceFileName;
	}

	/**
	 * Returns this preset's bundled resource file name.
	 *
	 * @since 2.1.5
	 * @author Luka Petrovic
	 * @return the resource file name under {@code policy/presets/}; never null.
	 */
	@Nonnull
	public String resourceFileName() {
		return resourceFileName;
	}
}
