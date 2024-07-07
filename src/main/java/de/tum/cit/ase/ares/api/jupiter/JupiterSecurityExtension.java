package de.tum.cit.ase.ares.api.jupiter;

import java.io.IOException;
import java.lang.annotation.AnnotationFormatError;
import java.nio.file.Path;
import java.util.Optional;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.policy.SecurityPolicy;
import de.tum.cit.ase.ares.api.policy.SecurityPolicyReader;
import de.tum.cit.ase.ares.api.policy.SupportedProgrammingLanguage;
import org.apiguardian.api.API;
import org.apiguardian.api.API.Status;
import org.junit.jupiter.api.extension.*;

import de.tum.cit.ase.ares.api.internal.ConfigurationUtils;

import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.checkFileAccess;
import static de.tum.cit.ase.ares.api.internal.TestGuardUtils.hasAnnotation;
import static org.junit.platform.commons.support.AnnotationSupport.findAnnotation;
//REMOVED: Import of ArtemisSecurityManager

@API(status = Status.INTERNAL)
public final class JupiterSecurityExtension implements UnifiedInvocationInterceptor {

    @Override
    public <T> T interceptGenericInvocation(Invocation<T> invocation, ExtensionContext extensionContext,
                                            Optional<ReflectiveInvocationContext<?>> invocationContext) throws Throwable {
        JupiterContext testContext = JupiterContext.of(extensionContext);
        if (hasAnnotation(testContext, Policy.class)) {
            SecurityPolicy securityPolicy = findAnnotation(testContext.testMethod(), Policy.class)
                    .map(policy -> {
                        try {
                            return SecurityPolicyReader.readSecurityPolicy(Path.of(policy.value()));
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    })
                    .orElseThrow(() -> new AnnotationFormatError("Policy annotation is missing"));
            if (securityPolicy.theProgrammingLanguageIUseInThisProgrammingExerciseIs() == SupportedProgrammingLanguage.JAVA) {
                checkFileAccess(securityPolicy.iAllowTheFollowingFileSystemInteractionsForTheStudents());
                //TODO: Add further checks
            } else {
                throw new UnsupportedOperationException("Only Java is supported by Ares.");
            }

        }

        var configuration = ConfigurationUtils.generateConfiguration(testContext);
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
}
