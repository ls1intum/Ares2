package de.tum.cit.ase.ares.integration.testuser;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.fileSystem.FileSystemAccessPenguin;

@UseLocale("en")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@StrictTimeout(value = 300, unit = TimeUnit.MILLISECONDS)
@TestMethodOrder(MethodName.class)
@WhitelistPath(value = "target/**", type = PathType.GLOB)
@SuppressWarnings("static-method")
public class FileSystemAccessUser {

	/* OUTCOMMENTED: Conceptually not possible anymore
	@PublicTest
	@WhitelistPath("")
	void accessPathAllFiles() {
		PathAccessPenguin.askForFilePermission("<<ALL FILES>>");
	}

	@PublicTest
	@WhitelistPath("")
	void accessPathAllowed() throws IOException {
		PathAccessPenguin.accessPath(Path.of("pom.xml"));
	}*/

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/pathaccess")
    public void accessPathNormalInstrumentation() throws IOException {
        //FileSystemAccessPenguin.accessPath(Path.of("pom212.xml"));
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/pathaccess")
    public void accessPathNormal() throws IOException {
        //FileSystemAccessPenguin.accessPath(Path.of("pom212.xml"));
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyOnePathAllowedRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/pathaccess")
    void accessPathNormalAllowed() throws IOException {
        //FileSystemAccessPenguin.accessPath(Path.of("pom.xml"));
    }

    @PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/pathaccess")
    void accessPathRelativeGlobDirectChildrenForbidden() {
        FileSystemAccessPenguin.askForFilePermission("*");
    }

    @PublicTest
    //@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/pathaccess")
    void accessPathRelativeGlobRecursiveForbidden() {
        FileSystemAccessPenguin.askForFilePermission("-");
    }

    /*@PublicTest
    @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/PolicyEverythingForbidden.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/architectureTests/thirdPartyAccess")
    void accessFileSystem() throws IOException {
        var x = 0;
        // do nothing
    }*/
//
//    @WhitelistPath(value = "../*r*e*s**", type = PathType.GLOB)
//    @PublicTest
//    void accessPathRelativeGlobA() throws IOException {
//        FileSystemAccessPenguin.accessPath(Path.of("pom.xml").toAbsolutePath());
//    }
//
//    @WhitelistPath(value = "./pom.xml", type = PathType.GLOB)
//    @PublicTest
//    void accessPathRelativeGlobB() throws IOException {
//        FileSystemAccessPenguin.accessPath(Path.of("pom.xml").toAbsolutePath());
//    }
//
//    @WhitelistPath(value = "../*r*e*s**", type = PathType.GLOB)
//    @PublicTest
//    void accessPathRelativeGlobDirectChildrenAllowed() {
//        FileSystemAccessPenguin.askForFilePermission("src/*");
//    }
//
//    @WhitelistPath(value = "../*r*e*s**", type = PathType.GLOB)
//    @BlacklistPath(value = "abc")
//    @PublicTest
//    void accessPathRelativeGlobDirectChildrenBlacklist() {
//        FileSystemAccessPenguin.askForFilePermission("*");
//    }
//
//
//    @WhitelistPath(value = "../*r*e*s**", type = PathType.GLOB)
//    @PublicTest
//    void accessPathRelativeGlobRecursiveAllowed() {
//        FileSystemAccessPenguin.askForFilePermission("-");
//    }
//
//    @WhitelistPath(value = "../*r*e*s**", type = PathType.GLOB)
//    @BlacklistPath(value = "abc")
//    @PublicTest
//    void accessPathRelativeGlobRecursiveBlacklist() {
//        FileSystemAccessPenguin.askForFilePermission("src/-");
//    }
//
//    @PublicTest
//    @WhitelistPath("")
//    @BlacklistPath(value = "**Test*.{java,class}", type = PathType.GLOB)
//    void accessPathTest() throws IOException {
//        Path file = Path.of("src/test/java/de/tum/cit/ase/ares/integration/SecurityTest.java");
//        if (!Files.exists(file))
//            fail("File not present: " + file.toAbsolutePath());
//        FileSystemAccessPenguin.accessPath(file);
//    }
//
//    @PublicTest
//    void weAccessPath() throws IOException {
//        Files.readString(Path.of("pom.xml"));
//    }
}
