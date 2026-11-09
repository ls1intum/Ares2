package de.tum.cit.ase.ares.api.internal;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import java.util.Locale;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;

import de.tum.cit.ase.ares.api.io.AresIOContext;
import de.tum.cit.ase.ares.api.io.IOManager;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ProgrammingLanguageConfiguration;
import de.tum.cit.ase.ares.api.policy.policySubComponents.ResourceAccesses;
import de.tum.cit.ase.ares.api.policy.policySubComponents.SupervisedCode;

/**
 * Checks the custom I/O manager contract: only the instructor's own test
 * classes, loaded from elsewhere than the student code, qualify.
 */
class IOManagerReferenceTest {

	/** This class's canonical name, listed as a test class. */
	private static final String THIS = IOManagerReferenceTest.class.getCanonicalName();

	/**
	 * A package compiled into the main output, standing in for student code, with a
	 * class there as its main class.
	 */
	private static final String STUDENT_PACKAGE = "de.tum.cit.ase.ares.api.policy.policySubComponents";

	/** The default locale before a test changed it. */
	private final Locale previous = Locale.getDefault();

	/** Restores the default locale. */
	@AfterEach
	void restoreLocale() {
		Locale.setDefault(previous);
	}

	/** A listed, concrete manager from the test output is accepted. */
	@Test
	void aListedManagerFromTheTestOutputIsAccepted() {
		assertThat(IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(THIS), STUDENT_PACKAGE, "PolicyDuration"), loader())).isEqualTo(RecordingManager.class);
	}

	/** A manager neither listed nor nested in a listed class is refused. */
	@Test
	void anUnlistedManagerIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(), STUDENT_PACKAGE, "PolicyDuration"), loader())).isInstanceOf(SecurityException.class)
						.hasMessageContaining("not listed in theFollowingClassesAreTestClasses");
	}

	/** A class that cannot be loaded is refused. */
	@Test
	void aMissingManagerIsRefused() {
		assertThatThrownBy(() -> IOManagerReference.resolve("org.example.Missing",
				policy(List.of("org.example.Missing"), STUDENT_PACKAGE, "PolicyDuration"), loader()))
						.isInstanceOf(SecurityException.class);
	}

	/** A manager loaded from where the student code is loaded from is refused. */
	@Test
	void aManagerFromTheStudentOutputIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(THIS), "de.tum.cit.ase.ares.api.internal", "SecurityViolationFeedbackTest"), loader()))
						.isInstanceOf(SecurityException.class).hasMessageContaining("where the student code is");
	}

	/** Without any student class to compare with, the manager is refused. */
	@Test
	void withoutAReferenceTheManagerIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(THIS), "org.nothing.here", null), loader())).isInstanceOf(SecurityException.class)
						.hasMessageContaining("cannot be checked");
	}

	/** A listed class that is not a usable manager is refused. */
	@Test
	void aClassOfTheWrongShapeIsRefused() {
		Locale.setDefault(Locale.ENGLISH);
		for (String shape : List.of(THIS + ".NotAManager", THIS + ".AbstractManager",
				THIS + ".ManagerWithoutDefaultConstructor")) {
			assertThatThrownBy(() -> IOManagerReference.resolve(shape,
					policy(List.of(THIS), STUDENT_PACKAGE, "PolicyDuration"), loader()))
							.isInstanceOf(SecurityException.class).hasMessageContaining("must be a concrete class");
		}
	}

	/** The refusal is localised in German. */
	@Test
	void theRefusalIsLocalisedInGerman() {
		Locale.setDefault(Locale.GERMAN);
		assertThatThrownBy(() -> IOManagerReference.resolve(THIS + ".RecordingManager",
				policy(List.of(), STUDENT_PACKAGE, "PolicyDuration"), loader()))
						.hasMessageContaining("Ares Sicherheitsfehler");
	}

	/**
	 * A policy part with the given test classes and student package.
	 *
	 * @param testClasses the listed test classes.
	 * @param packageName the supervised package.
	 * @param mainClass   the main class, or null.
	 * @return the policy part.
	 */
	private static SupervisedCode policy(List<String> testClasses, String packageName, String mainClass) {
		ResourceAccesses none = ResourceAccesses.builder().regardingFileSystemInteractions(List.of())
				.regardingNetworkConnections(List.of()).regardingCommandExecutions(List.of())
				.regardingThreadCreations(List.of()).regardingPackageImports(List.of()).build();
		return new SupervisedCode(ProgrammingLanguageConfiguration.JAVA_USING_MAVEN_ARCHUNIT_AND_ASPECTJ, packageName,
				mainClass, testClasses, none, null);
	}

	/** The loader of the test classes. */
	private static ClassLoader loader() {
		return IOManagerReferenceTest.class.getClassLoader();
	}

	/** A usable custom manager. */
	public static final class RecordingManager implements IOManager<Void> {

		/** Does nothing before a test. */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			// Nothing to prepare.
		}

		/** Does nothing after a test. */
		@Override
		public void afterTestExecution(AresIOContext context) {
			// Nothing to clean up.
		}

		/** Provides no controller. */
		@Override
		public Void getControllerInstance(AresIOContext context) {
			return null;
		}

		/** Provides no controller type. */
		@Override
		public Class<Void> getControllerClass() {
			return null;
		}
	}

	/** Not a manager at all. */
	public static final class NotAManager {
	}

	/** A manager that cannot be created. */
	public abstract static class AbstractManager implements IOManager<Void> {
	}

	/** A manager without a constructor without parameters. */
	public static final class ManagerWithoutDefaultConstructor implements IOManager<Void> {

		/**
		 * Needs an argument.
		 *
		 * @param ignored any value.
		 */
		ManagerWithoutDefaultConstructor(String ignored) {
		}

		/** Does nothing before a test. */
		@Override
		public void beforeTestExecution(AresIOContext context) {
			// Nothing to prepare.
		}

		/** Does nothing after a test. */
		@Override
		public void afterTestExecution(AresIOContext context) {
			// Nothing to clean up.
		}

		/** Provides no controller. */
		@Override
		public Void getControllerInstance(AresIOContext context) {
			return null;
		}

		/** Provides no controller type. */
		@Override
		public Class<Void> getControllerClass() {
			return null;
		}
	}
}
