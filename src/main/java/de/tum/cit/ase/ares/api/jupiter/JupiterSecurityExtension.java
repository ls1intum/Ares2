package de.tum.cit.ase.ares.api.jupiter;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.util.Optional;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReaderAndDirector;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.function.Try;


import static de.tum.cit.ase.ares.api.aop.java.instrumentation.advice.JavaInstrumentationAdviceFileSystemToolbox.localize;
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
            findAnnotation(testContext.testMethod(), Policy.class)
                    .ifPresent(policy -> SecurityPolicyReaderAndDirector.builder()
                            .securityPolicyFilePath(
                                    !policy.value().isBlank()
                                            ? JupiterSecurityExtension.testAndGetPolicyValue(policy)
                                            : null
                            )
                            .projectFolderPath(
                                    !policy.withinPath().isBlank()
                                            ? JupiterSecurityExtension.testAndGetPolicyWithinPath(policy)
                                            : Path.of("classes"))
                            .build()
                            .createTestCases()
                            .executeTestCases());
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
