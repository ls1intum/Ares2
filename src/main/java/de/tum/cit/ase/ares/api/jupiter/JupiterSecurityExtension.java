package de.tum.cit.ase.ares.api.jupiter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.dataformat.yaml.YAMLFactory;
import com.fasterxml.jackson.dataformat.yaml.YAMLMapper;
import com.google.common.base.Preconditions;
import de.tum.cit.ase.ares.api.Policy;
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
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.function.Try;

import javax.annotation.Nonnull;

import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.hasAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
//REMOVED: Import of ArtemisSecurityManager

@API(status = Status.INTERNAL)
public final class JupiterSecurityExtension implements UnifiedInvocationInterceptor {

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
    public <T> T interceptGenericInvocation(Invocation<T> invocation, ExtensionContext extensionContext,
                                            Optional<ReflectiveInvocationContext<?>> invocationContext) throws Throwable {
        JupiterContext testContext = JupiterContext.of(extensionContext);

        /**
         * Check if the test method has the {@link Policy} annotation. If it does, read
         * the policy file and run the security test cases.
         */
        if (hasAnnotation(testContext, Policy.class)) {
            Optional<Policy> policyAnnotation = findAnnotation(testContext.testMethod(), Policy.class);
            if (policyAnnotation.isPresent()) {
                SecurityPolicyReader securityPolicyReader = SecurityPolicyYAMLReader.builder()
                        .yamlMapper(new YAMLMapper())
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
            // We have to reset both the settings classes in the runtime and the bootstrap class loader to be able to run multiple tests in the same JVM instance.
            String className = "de.tum.cit.ase.ares.api.aop.java.JavaAOPTestCaseSettings";
            Try.call(() -> Class.forName(className, true, Thread.currentThread().getContextClassLoader()))
                    .ifSuccess(JupiterSecurityExtension::resetSettings);

            Try.call(() -> Class.forName(className, true, null))
                    .ifSuccess(JupiterSecurityExtension::resetSettings);
        }
        //REMOVED: Installing of ArtemisSecurityManager
        Throwable failure = null;
        try {
            return invocation.proceed();
        } catch (
                Throwable t) {
            failure = t;
        } finally {
            try {
                //REMOVED: Uninstallation of ArtemisSecurityManager
            } catch (Exception e) {
                if (failure == null)
                    failure = e;
                else
                    failure.addSuppressed(e);
            }
        }
        throw failure;
    }

    public static void resetSettings(Class<?> javaTestCaseSettingsClass) {
        try {
            Method resetMethod = javaTestCaseSettingsClass.getDeclaredMethod("reset");
            resetMethod.setAccessible(true);
            resetMethod.invoke(null);
            resetMethod.setAccessible(false);
        } catch (NoSuchMethodException e) {
            throw new SecurityException(localize("security.settings.reset.method.not.found"), e);
        } catch (IllegalAccessException e) {
            throw new SecurityException(localize("security.settings.reset.access.denied"), e);
        } catch (InvocationTargetException e) {
            throw new SecurityException(localize("security.settings.error.within.method"), e);
        }
    }

    public static Path testAndGetPolicyValue(Policy policyAnnotation) {
        String policyValue = policyAnnotation.value();
        if (policyValue.isBlank()) {
            throw new SecurityException(localize("security.policy.reader.path.blank"));
        }
        try {
            Path policyPath = Path.of(policyValue);
            if (!policyPath.toFile().exists()) {
                throw new SecurityException(localize("security.policy.reader.path.not.exists", policyPath));
            }
            return policyPath;
        } catch (InvalidPathException e) {
            throw new SecurityException(localize("security.policy.reader.path.invalid", policyValue));
        }
    }

    public static Path testAndGetPolicyWithinPath(Policy policyAnnotation) {
        String policyValue = policyAnnotation.withinPath();
        try {
            Path policyWithinPath = Path.of(policyValue);
            if (!policyWithinPath.startsWith("classes") && !policyWithinPath.startsWith("test-classes")) {
                throw new SecurityException(localize("security.policy.within.path.wrong.bytecode.path", policyValue));
            }
            return policyWithinPath;
        } catch (InvalidPathException e) {
            throw new SecurityException(localize("security.policy.within.path.invalid", policyValue));
        }
    }
}
