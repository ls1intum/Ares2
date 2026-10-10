package de.tum.cit.ase.ares.integration.testuser;

import static java.lang.annotation.ElementType.METHOD;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.Deadline;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.Hidden;
import de.tum.cit.ase.ares.api.jupiter.HiddenTest;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;

/** Mixed test classes exercising composed and inherited hidden methods. */
public final class SharedHiddenVisibilityUsers {

	/** Prevents construction of this fixture holder. */
	private SharedHiddenVisibilityUsers() {
	}

	/** A test annotation that makes a method hidden through a meta annotation. */
	@Retention(RUNTIME)
	@Target(METHOD)
	@Test
	@Hidden
	public @interface ComposedHiddenTest {
	}

	/** A hidden JUnit test inherited by an implementing class. */
	public interface InheritedHiddenTest {

		/** Emits a hidden result through an inherited default test. */
		@HiddenTest
		@Deadline("2000-01-01 00:00")
		default void inheritedHidden() {
		}
	}

	/** Public class with one hidden test declared through a composed annotation. */
	@Public
	@Policy(activated = false)
	public static class Composed {

		/** Shared output must be hidden because the composed method is hidden. */
		@BeforeAll
		static void sharedSetup() {
			System.out.print("SECRET_COMPOSED_SHARED");
		}

		/** A hidden method detected through its composed annotation. */
		@ComposedHiddenTest
		@Deadline("2000-01-01 00:00")
		void hidden() {
		}

		/** A public control method. */
		@PublicTest
		void visible() {
		}
	}

	/** Public class with a hidden test inherited from an interface. */
	@Public
	@Policy(activated = false)
	public static class Interface implements InheritedHiddenTest {

		/** Shared output must be hidden because the interface test is hidden. */
		@BeforeAll
		static void sharedSetup() {
			System.out.print("SECRET_INTERFACE_SHARED");
		}

		/** A public control method. */
		@PublicTest
		void visible() {
		}
	}
}
