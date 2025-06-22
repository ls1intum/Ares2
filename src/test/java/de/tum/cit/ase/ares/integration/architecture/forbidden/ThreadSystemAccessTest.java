package de.tum.cit.ase.ares.integration.architecture.forbidden;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.aop.forbidden.subject.threadSystem.create.executorService.CreateExecutorServiceMain;

public class ThreadSystemAccessTest {

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/instrumentation/PolicyOneThreadAllowedCreate.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/aop/forbidden/subject/threadSystem/create/executorServiceCreate")
    public void testExecutorServiceCreate() {
        CreateExecutorServiceMain.createExecutorService();
    }
}
