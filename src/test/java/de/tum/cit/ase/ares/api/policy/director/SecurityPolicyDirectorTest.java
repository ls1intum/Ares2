package de.tum.cit.ase.ares.api.policy.director;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.nio.file.Path;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.director.java.SecurityPolicyJavaDirector;
import de.tum.cit.ase.ares.api.securitytest.TestCaseAbstractFactoryAndBuilder;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml.EssentialDataYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;

class SecurityPolicyDirectorTest {

	/**
	 * The three-argument {@code createTestCases} overload must stay concrete. It
	 * shipped concrete in 2.1.0, so a subclass compiled against that release does
	 * not implement it and would break on an abstract declaration, at compile time
	 * for a rebuild and with an {@code AbstractMethodError} for an existing binary.
	 */
	@Test
	void withinPathOverloadStaysConcreteForSubclassesCompiledAgainstTheReleasedApi() throws NoSuchMethodException {
		Method overload = SecurityPolicyDirector.class.getDeclaredMethod("createTestCases", SecurityPolicy.class,
				Path.class, Path.class);
		assertFalse(Modifier.isAbstract(overload.getModifiers()));
	}

	/**
	 * The empty path is how an unscoped analysis is written throughout the policy
	 * layer, so the inherited default may forward it to the two-argument overload:
	 * nothing is lost.
	 */
	@Test
	void inheritedOverloadForwardsTheEmptyScope() {
		LegacyDirector director = new LegacyDirector();
		assertNotNull(director.createTestCases(null, Path.of("project"), Path.of("")));
		assertTrue(director.forwarded);
	}

	/**
	 * A real scope must not be forwarded. The default this method shipped with
	 * forwarded unconditionally and therefore dropped {@code withinPath}, so a
	 * subclass that never overrode it analysed the whole project while the caller
	 * had asked for one directory. Refusing is the fail-closed direction: analysing
	 * more than was asked for is the error that goes unnoticed.
	 */
	@Test
	void inheritedOverloadRefusesARealScopeRatherThanDroppingIt() {
		LegacyDirector director = new LegacyDirector();
		Path within = Path.of("src/main/java");
		assertThrows(UnsupportedOperationException.class, () -> director.createTestCases(null, Path.of("p"), within));
		assertFalse(director.forwarded);
	}

	@Test
	void inheritedOverloadRejectsANullScope() {
		LegacyDirector director = new LegacyDirector();
		assertThrows(NullPointerException.class, () -> director.createTestCases(null, Path.of("p"), null));
	}

	/**
	 * With no policy supplied, director selection must still yield the Java
	 * director so the no-policy path builds a (restrictive) run rather than
	 * failing.
	 */
	@Test
	void selectSecurityPolicyDirectorReturnsJavaDirectorWhenPolicyIsNull() {
		assertInstanceOf(SecurityPolicyJavaDirector.class, SecurityPolicyDirector.selectSecurityPolicyDirector(null));
	}

	/**
	 * Stands in for a director written against the 2.1.0 API: it implements only
	 * the two-argument overload and inherits the three-argument one.
	 */
	private static final class LegacyDirector extends SecurityPolicyDirector {

		private boolean forwarded;

		private LegacyDirector() {
			super(new JavaCreator(), new JavaWriter(), new JavaExecuter(), new EssentialDataYAMLReader(),
					new JavaProjectScanner(), Path.of("essential-packages.yaml"), Path.of("essential-classes.yaml"));
		}

		@Override
		public TestCaseAbstractFactoryAndBuilder createTestCases(SecurityPolicy securityPolicy, Path projectRootPath) {
			forwarded = true;
			return Mockito.mock(TestCaseAbstractFactoryAndBuilder.class);
		}
	}
}
