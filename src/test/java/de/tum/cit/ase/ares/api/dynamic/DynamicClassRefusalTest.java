package de.tum.cit.ase.ares.api.dynamic;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

/**
 * Checks that the reflection helpers refuse a wrong argument from the test code
 * with the Ares error template, as a {@link SecurityException}.
 */
class DynamicClassRefusalTest {

	/** Holds a primitive static field that is not final, for the setter test. */
	static final class Holder {

		/** A primitive static field the test tries to set to null. */
		static int count;

		/** Not instantiated: the test only uses the static field. */
		private Holder() {
		}
	}

	/** Null is refused when a class is compared. */
	@Test
	void isClassRefusesNull() {
		DynamicClass<?> dynamic = DynamicClass.toDynamic(String.class);
		SecurityException refused = assertThrows(SecurityException.class, () -> dynamic.isClass(null));
		assertEquals(localized("security.dynamic.argument.null", "isClass"), refused.getMessage());
	}

	/** An argument that names no class is refused when a class is compared. */
	@Test
	void isClassRefusesAnotherType() {
		DynamicClass<?> dynamic = DynamicClass.toDynamic(String.class);
		SecurityException refused = assertThrows(SecurityException.class, () -> dynamic.isClass(Integer.valueOf(1)));
		assertEquals(localized("security.dynamic.argument.type.invalid", "isClass", Integer.class),
				refused.getMessage());
	}

	/** Null is refused when a dynamic class is made. */
	@Test
	void toDynamicRefusesNull() {
		SecurityException refused = assertThrows(SecurityException.class, () -> DynamicClass.toDynamic((Object) null));
		assertEquals(localized("security.dynamic.argument.null", "toDynamic"), refused.getMessage());
	}

	/** An argument that names no class is refused when a dynamic class is made. */
	@Test
	void toDynamicRefusesAnotherType() {
		SecurityException refused = assertThrows(SecurityException.class,
				() -> DynamicClass.toDynamic((Object) Integer.valueOf(1)));
		assertEquals(localized("security.dynamic.argument.type.invalid", "toDynamic", Integer.class),
				refused.getMessage());
	}

	/** Null cannot be cast to a primitive class. */
	@Test
	void castRefusesNullForAPrimitive() {
		DynamicClass<Integer> primitive = DynamicClass.toDynamic(int.class);
		SecurityException refused = assertThrows(SecurityException.class, () -> primitive.cast(null));
		assertEquals(localized("dynamics.class.null", "int"), refused.getMessage());
	}

	/**
	 * Setting a primitive static field to null names the failed cast, rather than
	 * claiming the field is not static.
	 */
	@Test
	void setStaticToNullOnAPrimitiveNamesTheCast() {
		DynamicField<Integer> field = DynamicClass.toDynamic(Holder.class).field(int.class, "count");
		SecurityException refused = assertThrows(SecurityException.class, () -> field.setStatic(null));
		assertEquals(localized("dynamics.class.null", "int"), refused.getMessage());
	}
}
