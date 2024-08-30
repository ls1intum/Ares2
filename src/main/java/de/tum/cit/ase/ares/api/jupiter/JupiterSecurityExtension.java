package de.tum.cit.ase.ares.api.jupiter;

import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.aspectconfiguration.javaaspectj.JavaAspectJConfigurationSettings;
import de.tum.cit.ase.ares.api.aspectconfiguration.javainstrumentation.JavaInstrumentationConfigurationSettings;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;

import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.hasAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
//REMOVED: Import of ArtemisSecurityManager

@API(status = Status.INTERNAL)
public final class JupiterSecurityExtension implements UnifiedInvocationInterceptor {

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
                Path policyPath = JupiterSecurityExtension.testAndGetPolicyValue(policyAnnotation.get());
                if (!policyAnnotation.get().withinPath().isBlank()) {
                    Path withinPath = JupiterSecurityExtension.testAndGetPolicyWithinPath(policyAnnotation.get());
                    new SecurityPolicyReaderAndDirector(policyPath, withinPath).runSecurityTestCases();
                } else {
                    new SecurityPolicyReaderAndDirector(policyPath, Path.of("classes")).runSecurityTestCases();
                }
            }
        } else {
            JavaAspectJConfigurationSettings.reset();
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

    public static Path testAndGetPolicyValue(Policy policyAnnotation) {
        String policyValue = policyAnnotation.value();
        if (policyValue.isBlank()) {
            throw new SecurityException("The policy file path is not specified.");
        }
        try {
            Path policyPath = Path.of(policyValue);
            if (!policyPath.toFile().exists()) {
                throw new SecurityException("The following policy file path does not exist: " + policyPath);
            }
            return policyPath;
        } catch (InvalidPathException e) {
            throw new SecurityException("The following policy file path is invalid: " + policyValue);
        }
    }

    public static Path testAndGetPolicyWithinPath(Policy policyAnnotation) {
        String policyValue = policyAnnotation.withinPath();
        try {
            Path policyWithinPath = Path.of(policyValue);
            if (!policyWithinPath.startsWith("classes") && !policyWithinPath.startsWith("test-classes")) {
                throw new SecurityException("The following path is invalid for withinPath it should start with classes or test-classes: " + policyValue);
            }
            return policyWithinPath;
        } catch (InvalidPathException e) {
            throw new SecurityException("The following path is invalid for withinPath: " + policyValue);
        }
    }
}
