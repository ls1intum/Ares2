package de.tum.cit.ase.ares.api.jqwik;

import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import de.tum.cit.ase.ares.api.policy.director.SecurityPolicyDirector;
import de.tum.cit.ase.ares.api.policy.director.java.SecurityPolicyJavaDirector;
import de.tum.cit.ase.ares.api.policy.reader.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.policy.reader.yaml.SecurityPolicyYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.creator.JavaCreator;
import de.tum.cit.ase.ares.api.securitytest.java.essentialModel.yaml.EssentialDataYAMLReader;
import de.tum.cit.ase.ares.api.securitytest.java.executer.JavaExecuter;
import de.tum.cit.ase.ares.api.securitytest.java.projectScanner.JavaProjectScanner;
import de.tum.cit.ase.ares.api.securitytest.java.writer.JavaWriter;
import de.tum.cit.ase.ares.api.util.FileTools;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;

import net.jqwik.api.lifecycle.*;

import javax.annotation.Nonnull;

import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.hasAnnotation;
import static de.tum.cit.ase.ares.api.jupiter.JupiterSecurityExtension.resetSettings;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
//REMOVED: Import of ArtemisSecurityManager

/**
 * <p>
 * <i>Adaption for jqwik.</i>
 *
 * @author Christian Femers
 */
@API(status = Status.INTERNAL)
public final class JqwikSecurityExtension implements AroundPropertyHook {

	/**
	 * Default path to the essential packages YAML file.
	 */
	@Nonnull
	private static final Path DEFAULT_ESSENTIAL_PACKAGES_PATH = Preconditions.checkNotNull(
			FileTools.resolveOnPackage("configuration/EssentialPackages.yaml")
	);

	/**
	 * Default path to the essential classes YAML file.
	 */
	@Nonnull
	private static final Path DEFAULT_ESSENTIAL_CLASSES_PATH = Preconditions.checkNotNull(
			FileTools.resolveOnPackage("configuration/EssentialClasses.yaml")
	);

	@Override
	public int aroundPropertyProximity() {
		return 30;
	}

	@Override
	public PropertyExecutionResult aroundProperty(PropertyLifecycleContext context, PropertyExecutor property) {
		var testContext = JqwikContext.of(context);
		/*
		 * Check if the test method has the {@link Policy} annotation. If it does, read
		 * the policy file and run the security test cases.
		 */
		if (hasAnnotation(testContext, Policy.class)) {
			Optional<Policy> policyAnnotation = findAnnotation(testContext.testMethod(), Policy.class);
			if (policyAnnotation.isPresent()) {
				SecurityPolicyReader securityPolicyReader = SecurityPolicyYAMLReader.builder()
						.yamlMapper((YAMLMapper) new ObjectMapper(new YAMLFactory()))
						.build();
				SecurityPolicyDirector securityPolicyDirector = SecurityPolicyJavaDirector.builder()
						.creator(new JavaCreator())
						.writer(new JavaWriter())
						.executer(new JavaExecuter())
						.essentialDataReader(new EssentialDataYAMLReader())
						.javaScanner(new JavaProjectScanner())
						.essentialPackagesPath(DEFAULT_ESSENTIAL_PACKAGES_PATH)
						.essentialClassesPath(DEFAULT_ESSENTIAL_CLASSES_PATH)
						.build();
				Path securityPolicyFilePath = JupiterSecurityExtension.testAndGetPolicyValue(policyAnnotation.get());
				if (!policyAnnotation.get().withinPath().isBlank()) {
					Path projectFolderPath = JupiterSecurityExtension.testAndGetPolicyWithinPath(policyAnnotation.get());
					SecurityPolicyReaderAndDirector securityPolicyReaderAndDirector = SecurityPolicyReaderAndDirector.builder()
							.securityPolicyReader(securityPolicyReader)
							.securityPolicyDirector(securityPolicyDirector)
							.securityPolicyFilePath(securityPolicyFilePath)
							.projectFolderPath(projectFolderPath)
							.build();
					securityPolicyReaderAndDirector.executeTestCases();
				} else {
					SecurityPolicyReaderAndDirector securityPolicyReaderAndDirector = SecurityPolicyReaderAndDirector.builder()
							.securityPolicyReader(securityPolicyReader)
							.securityPolicyDirector(securityPolicyDirector)
							.securityPolicyFilePath(securityPolicyFilePath)
							.projectFolderPath(Path.of("classes"))
							.build();
					securityPolicyReaderAndDirector.executeTestCases();
				}
			}
		} else {
			try {
				Class<?> javaTestCaseSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings");
				resetSettings(javaTestCaseSettingsClass);
				javaTestCaseSettingsClass = Class.forName("de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings", true, null);
				resetSettings(javaTestCaseSettingsClass);
			} catch (ClassNotFoundException e) {
				throw new SecurityException("Security configuration error: The class for the specific security test case settings could not be found. Ensure the class name is correct and the class is available at runtime.", e);
			}
		}
//REMOVED: Installing of ArtemisSecurityManager
		PropertyExecutionResult result;
		Throwable error = null;
		try {
			/*
			 * Note that the only Throwable not caught and collected is OutOfMemoryError
			 */
			result = property.execute();
		} finally {
			try {
				//REMOVED: InInstallation of ArtemisSecurityManager
			} catch (Exception e) {
				error = e;
			}
		}
		// Fix for issue #1, add as suppressed exception
		if (error != null) {
			Optional<Throwable> propExecError = result.throwable();
			if (propExecError.isPresent())
				propExecError.get().addSuppressed(error);
			else
				result = result.mapToFailed(error);
		}
		return result;
	}
}
