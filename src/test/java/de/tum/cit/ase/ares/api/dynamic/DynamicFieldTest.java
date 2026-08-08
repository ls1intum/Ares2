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
	}

	interface ExtendingInterface extends FieldOwningInterface {
		// declares no fields of its own: CONSTANT is only reachable via
		// FieldOwningInterface
	}

	static class ImplementingClass implements ExtendingInterface {
		// declares no fields of its own
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
