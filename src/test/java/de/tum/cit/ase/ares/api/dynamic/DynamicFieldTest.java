package de.tum.cit.ase.ares.api.dynamic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatCode;

import org.junit.jupiter.api.Test;

/**
 * Regression tests for {@link DynamicField}'s private {@code fieldsOf}
 * traversal, guarding against I-094: {@code Class.getSuperclass()} returns
 * {@code null} for an interface, which previously made the superclass-only walk
 * NPE once it reached an interface, and the walk never visited
 * implemented/extended superinterfaces at all, so an inherited interface
 * constant could never be found.
 */
class DynamicFieldTest {

	interface FieldOwningInterface {
		int CONSTANT = 42;

		/**
		 * Gives the interface a method, so it describes a type rather than only holding
		 * a constant.
		 *
		 * @return the constant
		 */
		default int constant() {
			return CONSTANT;
		}
	}

	interface ExtendingInterface extends FieldOwningInterface {
		// declares no fields of its own: CONSTANT is only reachable via
		// FieldOwningInterface
	}

	static class ImplementingClass implements ExtendingInterface {
		// declares no fields of its own
	}

	/** Declares the field {@link Child} inherits through {@link Parent}. */
	static class Grandparent {
		/** The superclass field a lookup on {@link Child} must find. */
		int LIMIT = 1;
	}

	/** Passes {@link Grandparent}'s field on without declaring one. */
	static class Parent extends Grandparent {
		// declares no fields of its own: LIMIT comes from Grandparent
	}

	/** Declares a constant with the same name as {@link Grandparent}'s field. */
	interface LimitOwningInterface {
		/** The interface constant of the same name, which must not win. */
		int LIMIT = 2;

		/**
		 * Gives the interface a method, so it describes a type rather than only holding
		 * a constant.
		 *
		 * @return the constant
		 */
		default int limit() {
			return LIMIT;
		}
	}

	/** Sees the same field name on a superclass and on an interface. */
	static class Child extends Parent implements LimitOwningInterface {
		// declares no fields of its own: LIMIT is found on Grandparent and on the
		// interface
	}

	/**
	 * A field of the same name on a superclass further up wins over a constant of
	 * an interface, as it did before interfaces were searched at all.
	 */
	@Test
	void superclassFieldWinsOverAnInterfaceConstantOfTheSameName() {
		var field = DynamicClass.toDynamic(Child.class).field(int.class, "LIMIT");
		var child = new Child();
		assertThat(field.getOf(child)).isEqualTo(1);
		field.setOf(child, 3);
		assertThat(field.getOf(child)).isEqualTo(3);
	}

	@Test
	void fieldDeclaredDirectlyOnInterfaceIsFound() {
		var field = DynamicClass.toDynamic(FieldOwningInterface.class).field(int.class, "CONSTANT");
		assertThat(field.exists()).isTrue();
		assertThat(field.getStatic()).isEqualTo(42);
	}

	@Test
	void fieldInheritedFromSuperinterfaceIsFound() {
		var field = DynamicClass.toDynamic(ExtendingInterface.class).field(int.class, "CONSTANT");
		assertThat(field.exists()).isTrue();
		assertThat(field.getStatic()).isEqualTo(42);
	}

	@Test
	void fieldInheritedFromImplementedInterfaceIsFoundOnClass() {
		var field = DynamicClass.toDynamic(ImplementingClass.class).field(int.class, "CONSTANT");
		assertThat(field.exists()).isTrue();
		assertThat(field.getStatic()).isEqualTo(42);
	}

	@Test
	void lookupStartingFromAnInterfaceDoesNotThrowOnNullSuperclass() {
		var field = DynamicClass.toDynamic(FieldOwningInterface.class).field(int.class, "doesNotExist");
		assertThatCode(field::exists).doesNotThrowAnyException();
		assertThat(field.exists()).isFalse();
	}
}
