package de.tum.cit.ase.ares.integration.testuser;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.jupiter.Public;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.TestMethodOrder;

import java.io.IOException;

@Public
@UseLocale("en")
@AllowThreads(maxActiveCount = 100)
@MirrorOutput(MirrorOutput.MirrorOutputPolicy.DISABLED)
@StrictTimeout(5)
@TestMethodOrder(MethodOrderer.MethodName.class)
@WhitelistPath(value = "target/**", type = PathType.GLOB)
@BlacklistPath(value = "**Test*.{java,class}", type = PathType.GLOB)
public class CommandExecutionUser {

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/EverythingForbiddenPolicy.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/commandexecution")
    void testExecuteCommand() {
        // do nothing
    }
}
