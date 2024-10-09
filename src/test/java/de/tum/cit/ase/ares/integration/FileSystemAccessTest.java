package de.tum.cit.ase.ares.integration;

import static de.tum.cit.ase.ares.testutilities.CustomConditions.*;
import static org.junit.jupiter.api.Assertions.fail;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin;
import org.junit.jupiter.api.Nested;
import org.junit.platform.testkit.engine.Events;

import de.tum.cit.ase.ares.integration.testuser.FileSystemAccessUser;
import de.tum.cit.ase.ares.testutilities.*;

import java.io.IOException;

@UserBased(FileSystemAccessUser.class)
class FileSystemAccessTest {

    @UserTestResults
    private static Events tests;

    //<editor-fold desc="Constants">
    private final String accessPathAllFiles = "accessPathAllFiles";
    private final String accessPathAllowed = "accessPathAllowed";
    private final String accessPathNormal = "accessPathNormal";
    private final String accessPathNormalAllowed = "accessPathNormalAllowed";
    private final String accessPathRelativeGlobA = "accessPathRelativeGlobA";
    private final String accessPathRelativeGlobB = "accessPathRelativeGlobB";
    private final String accessPathRelativeGlobDirectChildrenAllowed = "accessPathRelativeGlobDirectChildrenAllowed";
    private final String accessPathRelativeGlobDirectChildrenBlacklist = "accessPathRelativeGlobDirectChildrenBlacklist";
    private final String accessPathRelativeGlobDirectChildrenForbidden = "accessPathRelativeGlobDirectChildrenForbidden";
    private final String accessPathRelativeGlobRecursiveAllowed = "accessPathRelativeGlobRecursiveAllowed";
    private final String accessPathRelativeGlobRecursiveBlacklist = "accessPathRelativeGlobRecursiveBlacklist";
    private final String accessPathRelativeGlobRecursiveForbidden = "accessPathRelativeGlobRecursiveForbidden";
    private final String accessPathTest = "accessPathTest";
    private final String weAccessPath = "weAccessPath";

    private final String errorMessage = "No Security Exception was thrown. Check if the policy is correctly applied.";
    //</editor-fold>

    // TODO Markus: Look into why we cannot structure the rest of the tests like "Other Tests"
    //<editor-fold desc="Other Tests">
	/* OUTCOMMENTED: Conceptually not possible anymore
	@TestTest
	void test_accessPathAllFiles() {
		tests.assertThatEvents().haveExactly(1, testFailedWith(accessPathAllFiles, SecurityException.class));
	}

	@TestTest
	void test_accessPathAllowed() {
		tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathAllowed));
	}
	*/

    @TestTest
    void test_accessPathAllowed() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathNormalAllowed));
    }

    @TestTest
    void test_accessPathRelativeGlobA() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobA));
    }

    @TestTest
    void test_accessPathRelativeGlobB() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobB));
    }

    @TestTest
    void test_accessPathRelativeGlobDirectChildrenAllowed() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobDirectChildrenAllowed));
    }

    @TestTest
    void test_accessPathRelativeGlobDirectChildrenBlacklist() {
        //OUTCOMMENTED: Test does not pass
        //tests.assertThatEvents().haveExactly(1,
        //		testFailedWith(accessPathRelativeGlobDirectChildrenBlacklist, SecurityException.class));
    }

    @TestTest
    void test_accessPathRelativeGlobDirectChildrenForbidden() {
        // tests.assertThatEvents().haveExactly(1,
        //		testFailedWith(accessPathRelativeGlobDirectChildrenForbidden, SecurityException.class));
    }

    @TestTest
    void test_accessPathRelativeGlobRecursiveAllowed() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(accessPathRelativeGlobRecursiveAllowed));
    }

    @TestTest
    void test_accessPathRelativeGlobRecursiveBlacklist() {
        //OUTCOMMENTED: Test does not pass
        //tests.assertThatEvents().haveExactly(1,
        //		testFailedWith(accessPathRelativeGlobRecursiveBlacklist, SecurityException.class));
    }

    @TestTest
    void test_accessPathRelativeGlobRecursiveForbidden() {
        //tests.assertThatEvents().haveExactly(1,
        //		testFailedWith(accessPathRelativeGlobRecursiveForbidden, SecurityException.class));
    }

    @TestTest
    void test_accessPathTest() {
        //OUTCOMMENTED: Test does not pass
        //tests.assertThatEvents().haveExactly(1, testFailedWith(accessPathTest, SecurityException.class));
    }

    @TestTest
    void test_weAccessPath() {
        tests.assertThatEvents().haveExactly(1, finishedSuccessfully(weAccessPath));
    }

    @TestTest
    void test_accessFileSystem() {
        tests.assertThatEvents().haveExactly(1, testFailedWith("accessFileSystem", SecurityException.class,
                """
                       Ares Security Error (Reason: Student-Code; Stage: Execution): Illegal Statement found: Architecture Violation [Priority: MEDIUM] - Rule 'no classes should transitively depend on classes that accesses file system' was violated (65 times):
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaBufferedReader()> calls constructor <java.io.FileReader.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:84) accesses <java.io.FileReader.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:84)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaBufferedWriter()> calls constructor <java.io.FileWriter.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:182) accesses <java.io.FileWriter.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:182)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaDataInputStream()> calls constructor <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:101) accesses <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:101)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaDataOutputStream()> calls constructor <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:198) accesses <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:198)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaDesktop()> calls constructor <java.io.File.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:270) accesses <java.io.File.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:270)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaDesktop()> calls method <java.awt.Desktop.open(java.io.File)> in (FileSystemAccessPenguin.java:270) accesses <java.awt.Desktop.open(java.io.File)> in (FileSystemAccessPenguin.java:270)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileChannelRead()> calls method <java.nio.channels.FileChannel.open(java.nio.file.Path, [Ljava.nio.file.OpenOption;)> in (FileSystemAccessPenguin.java:125) accesses <java.nio.channels.FileChannel.open(java.nio.file.Path, [Ljava.nio.file.OpenOption;)> in (FileSystemAccessPenguin.java:125)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileChannelRead()> calls method <java.nio.channels.FileChannel.read(java.nio.ByteBuffer)> in (FileSystemAccessPenguin.java:126) accesses <java.nio.channels.FileChannel.read(java.nio.ByteBuffer)> in (FileSystemAccessPenguin.java:126)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileChannelWrite()> calls method <java.nio.channels.FileChannel.open(java.nio.file.Path, [Ljava.nio.file.OpenOption;)> in (FileSystemAccessPenguin.java:230) accesses <java.nio.channels.FileChannel.open(java.nio.file.Path, [Ljava.nio.file.OpenOption;)> in (FileSystemAccessPenguin.java:230)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileChannelWrite()> calls method <java.nio.channels.FileChannel.write(java.nio.ByteBuffer)> in (FileSystemAccessPenguin.java:231) accesses <java.nio.channels.FileChannel.write(java.nio.ByteBuffer)> in (FileSystemAccessPenguin.java:231)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileExecute()> calls constructor <java.io.File.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:259) accesses <java.io.File.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:259)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileExecute()> calls method <java.io.File.canExecute()> in (FileSystemAccessPenguin.java:260) accesses <java.io.File.canExecute()> in (FileSystemAccessPenguin.java:260)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileExecute()> calls method <java.io.File.setExecutable(boolean)> in (FileSystemAccessPenguin.java:261) accesses <java.io.File.setExecutable(boolean)> in (FileSystemAccessPenguin.java:261)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileExecute()> calls method <java.nio.file.Files.isExecutable(java.nio.file.Path)> in (FileSystemAccessPenguin.java:262) accesses <java.nio.file.Files.isExecutable(java.nio.file.Path)> in (FileSystemAccessPenguin.java:262)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileHandler()> calls constructor <java.util.logging.FileHandler.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:246) accesses <java.util.logging.FileHandler.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:246)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileHandler()> calls method <java.util.logging.FileHandler.close()> in (FileSystemAccessPenguin.java:248) accesses <java.util.logging.FileHandler.close()> in (FileSystemAccessPenguin.java:248)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileInputStream()> calls constructor <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:69) accesses <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:69)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileInputStream()> calls method <java.io.FileInputStream.read()> in (FileSystemAccessPenguin.java:69) accesses <java.io.FileInputStream.read()> in (FileSystemAccessPenguin.java:69)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileOutputStream()> calls constructor <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:164) accesses <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:164)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileOutputStream()> calls method <java.io.FileOutputStream.write(int)> in (FileSystemAccessPenguin.java:164) accesses <java.io.FileOutputStream.write(int)> in (FileSystemAccessPenguin.java:164)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls constructor <java.io.File.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:51) accesses <java.io.File.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:51)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.canRead()> in (FileSystemAccessPenguin.java:52) accesses <java.io.File.canRead()> in (FileSystemAccessPenguin.java:52)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.exists()> in (FileSystemAccessPenguin.java:53) accesses <java.io.File.exists()> in (FileSystemAccessPenguin.java:53)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.getFreeSpace()> in (FileSystemAccessPenguin.java:54) accesses <java.io.File.getFreeSpace()> in (FileSystemAccessPenguin.java:54)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.getTotalSpace()> in (FileSystemAccessPenguin.java:55) accesses <java.io.File.getTotalSpace()> in (FileSystemAccessPenguin.java:55)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.getUsableSpace()> in (FileSystemAccessPenguin.java:56) accesses <java.io.File.getUsableSpace()> in (FileSystemAccessPenguin.java:56)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.isDirectory()> in (FileSystemAccessPenguin.java:57) accesses <java.io.File.isDirectory()> in (FileSystemAccessPenguin.java:57)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.isFile()> in (FileSystemAccessPenguin.java:58) accesses <java.io.File.isFile()> in (FileSystemAccessPenguin.java:58)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.isHidden()> in (FileSystemAccessPenguin.java:59) accesses <java.io.File.isHidden()> in (FileSystemAccessPenguin.java:59)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.lastModified()> in (FileSystemAccessPenguin.java:60) accesses <java.io.File.lastModified()> in (FileSystemAccessPenguin.java:60)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.length()> in (FileSystemAccessPenguin.java:61) accesses <java.io.File.length()> in (FileSystemAccessPenguin.java:61)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileRead()> calls method <java.io.File.listFiles()> in (FileSystemAccessPenguin.java:62) accesses <java.io.File.listFiles()> in (FileSystemAccessPenguin.java:62)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileReader()> calls constructor <java.io.FileReader.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:76) accesses <java.io.FileReader.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:76)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileReader()> calls method <java.io.FileReader.read()> in (FileSystemAccessPenguin.java:77) accesses <java.io.FileReader.read()> in (FileSystemAccessPenguin.java:77)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWrite()> calls constructor <java.io.File.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:152) accesses <java.io.File.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:152)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWrite()> calls method <java.io.File.canWrite()> in (FileSystemAccessPenguin.java:153) accesses <java.io.File.canWrite()> in (FileSystemAccessPenguin.java:153)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWrite()> calls method <java.io.File.createNewFile()> in (FileSystemAccessPenguin.java:154) accesses <java.io.File.createNewFile()> in (FileSystemAccessPenguin.java:154)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWrite()> calls method <java.io.File.createTempFile(java.lang.String, java.lang.String)> in (FileSystemAccessPenguin.java:155) accesses <java.io.File.createTempFile(java.lang.String, java.lang.String)> in (FileSystemAccessPenguin.java:155)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWrite()> calls method <java.io.File.setReadable(boolean)> in (FileSystemAccessPenguin.java:156) accesses <java.io.File.setReadable(boolean)> in (FileSystemAccessPenguin.java:156)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWrite()> calls method <java.io.File.setWritable(boolean)> in (FileSystemAccessPenguin.java:157) accesses <java.io.File.setWritable(boolean)> in (FileSystemAccessPenguin.java:157)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWriter()> calls constructor <java.io.FileWriter.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:171) accesses <java.io.FileWriter.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:171)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWriter()> calls method <java.io.FileWriter.append(char)> in (FileSystemAccessPenguin.java:173) accesses <java.io.FileWriter.append(char)> in (FileSystemAccessPenguin.java:173)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWriter()> calls method <java.io.FileWriter.close()> in (FileSystemAccessPenguin.java:175) accesses <java.io.FileWriter.close()> in (FileSystemAccessPenguin.java:175)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWriter()> calls method <java.io.FileWriter.flush()> in (FileSystemAccessPenguin.java:174) accesses <java.io.FileWriter.flush()> in (FileSystemAccessPenguin.java:174)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFileWriter()> calls method <java.io.FileWriter.write(int)> in (FileSystemAccessPenguin.java:172) accesses <java.io.FileWriter.write(int)> in (FileSystemAccessPenguin.java:172)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFilesDelete()> calls method <java.nio.file.Files.delete(java.nio.file.Path)> in (FileSystemAccessPenguin.java:281) accesses <java.nio.file.Files.delete(java.nio.file.Path)> in (FileSystemAccessPenguin.java:281)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFilesRead()> calls method <java.nio.file.Files.readAllBytes(java.nio.file.Path)> in (FileSystemAccessPenguin.java:133) accesses <java.nio.file.Files.readAllBytes(java.nio.file.Path)> in (FileSystemAccessPenguin.java:133)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFilesWrite()> calls method <java.nio.file.Files.createFile(java.nio.file.Path, [Ljava.nio.file.attribute.FileAttribute;)> in (FileSystemAccessPenguin.java:239) accesses <java.nio.file.Files.createFile(java.nio.file.Path, [Ljava.nio.file.attribute.FileAttribute;)> in (FileSystemAccessPenguin.java:239)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaFilesWrite()> calls method <java.nio.file.Files.write(java.nio.file.Path, [B, [Ljava.nio.file.OpenOption;)> in (FileSystemAccessPenguin.java:238) accesses <java.nio.file.Files.write(java.nio.file.Path, [B, [Ljava.nio.file.OpenOption;)> in (FileSystemAccessPenguin.java:238)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaInputStreamReader()> calls constructor <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:117) accesses <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:117)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaJFileChooser()> calls constructor <javax.swing.JFileChooser.<init>()> in (FileSystemAccessPenguin.java:297) accesses <javax.swing.JFileChooser.<init>()> in (FileSystemAccessPenguin.java:297)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaJFileChooser()> calls method <javax.swing.JFileChooser.showOpenDialog(java.awt.Component)> in (FileSystemAccessPenguin.java:298) accesses <javax.swing.JFileChooser.showOpenDialog(java.awt.Component)> in (FileSystemAccessPenguin.java:298)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaObjectInputStream()> calls constructor <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:109) accesses <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:109)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaObjectOutputStream()> calls constructor <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:214) accesses <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:214)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaOutputStreamWriter()> calls constructor <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:206) accesses <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:206)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaPrintStream()> calls constructor <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:222) accesses <java.io.FileOutputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:222)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaPrintWriter()> calls constructor <java.io.PrintWriter.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:190) accesses <java.io.PrintWriter.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:190)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaRandomAccessFile()> calls constructor <java.io.RandomAccessFile.<init>(java.lang.String, java.lang.String)> in (FileSystemAccessPenguin.java:43) accesses <java.io.RandomAccessFile.<init>(java.lang.String, java.lang.String)> in (FileSystemAccessPenguin.java:43)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaRandomAccessFile()> calls method <java.io.RandomAccessFile.read()> in (FileSystemAccessPenguin.java:44) accesses <java.io.RandomAccessFile.read()> in (FileSystemAccessPenguin.java:44)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaRandomAccessFileWrite()> calls constructor <java.io.RandomAccessFile.<init>(java.lang.String, java.lang.String)> in (FileSystemAccessPenguin.java:144) accesses <java.io.RandomAccessFile.<init>(java.lang.String, java.lang.String)> in (FileSystemAccessPenguin.java:144)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaRandomAccessFileWrite()> calls method <java.io.RandomAccessFile.write(int)> in (FileSystemAccessPenguin.java:145) accesses <java.io.RandomAccessFile.write(int)> in (FileSystemAccessPenguin.java:145)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaScanner()> calls constructor <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:93) accesses <java.io.FileInputStream.<init>(java.lang.String)> in (FileSystemAccessPenguin.java:93)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessFileSystemViaScanner()> calls constructor <java.util.zip.ZipInputStream.<init>(java.io.InputStream)> in (FileSystemAccessPenguin.java:93) accesses <java.util.zip.ZipInputStream.<init>(java.io.InputStream)> in (FileSystemAccessPenguin.java:93)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessPath(java.nio.file.Path)> calls method <java.nio.file.Files.readString(java.nio.file.Path)> in (FileSystemAccessPenguin.java:27) accesses <java.nio.file.Files.readString(java.nio.file.Path)> in (FileSystemAccessPenguin.java:27)
                       Method <de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessPathThroughThirdPartyPackage()> calls method <de.tum.cit.ase.ares.integration.testuser.subject.thirdpartypackage.ThirdPartyPackagePenguin.accessFileSystem()> in (FileSystemAccessPenguin.java:303) transitively accesses <java.nio.file.Files.readString(java.nio.file.Path)> by [de.tum.cit.ase.ares.integration.testuser.subject.fileSystem.FileSystemAccessPenguin.accessPathThroughThirdPartyPackage()->de.tum.cit.ase.ares.integration.testuser.subject.thirdpartypackage.ThirdPartyPackagePenguin.accessFileSystem()] in (FileSystemAccessPenguin.java:303)"""));
    }
    //</editor-fold>

    //TODO Markus: Look into why we are catching Runtime Exceptions and not Security Exceptions

    //<editor-fold desc="Read Operations">
    // --- Read Operations ---
    @Nested
    class ReadOperations {

        //<editor-fold desc="accessFileSystemViaRandomAccessFileRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileReadAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFile();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileReadInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFile();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReadAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReadInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileInputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileInputStreamInstrumentation() throws IOException{
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReaderAspectJ() throws IOException{
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileReaderInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedReaderAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedReaderInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaScanner">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaScannerAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaScanner();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaScannerInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaScanner();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDataInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataInputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataInputStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaObjectInputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectInputStreamAspectJ() throws IOException, ClassNotFoundException{
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectInputStreamInstrumentation() throws IOException, ClassNotFoundException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectInputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaInputStreamReader">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaInputStreamReaderAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaInputStreamReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaInputStreamReaderInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaInputStreamReader();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileChannelRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelReadAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelReadInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFilesRead">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesReadAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationRead.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesReadInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesRead();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Write Operations">
    // --- Write Operations ---
    @Nested
    class WriteOperations {

        //<editor-fold desc="accessFileSystemViaRandomAccessFileWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileWriteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFileWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaRandomAccessFileWriteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaRandomAccessFileWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileOutputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileOutputStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriterAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileWriterInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaBufferedWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedWriterAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaBufferedWriterInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaBufferedWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaPrintWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintWriterAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintWriterInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDataOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataOutputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDataOutputStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDataOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaObjectOutputStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectOutputStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaObjectOutputStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaObjectOutputStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaOutputStreamWriter">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaOutputStreamWriterAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaOutputStreamWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaOutputStreamWriterInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaOutputStreamWriter();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaPrintStream">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintStreamAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaPrintStreamInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaPrintStream();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileChannelWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelWriteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileChannelWriteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileChannelWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFilesWrite">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesWriteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesWriteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesWrite();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileHandler">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileHandlerAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileHandler();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationWrite.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileHandlerInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileHandler();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Execute Operations">
    // --- Execute Operations ---
    @Nested
    class ExecuteOperations {

        //<editor-fold desc="accessFileSystemViaFileExecute">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileExecuteAspectJ() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileExecute();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileExecuteInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileExecute();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaDesktop">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDesktopAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDesktop();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationExecute.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaDesktopInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaDesktop();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>
    }
    //</editor-fold>

    //<editor-fold desc="Delete Operations">
    // --- Delete Operations ---
    @Nested
    class DeleteOperations {

        //<editor-fold desc="accessFileSystemViaFilesDelete">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesDeleteAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesDelete();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFilesDeleteInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFilesDelete();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaFileSystemProvider">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileSystemProviderAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileSystemProvider();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaFileSystemProviderInstrumentation() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileSystemProvider();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>

        //<editor-fold desc="accessFileSystemViaJFileChooser">
        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedAspectJDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaJFileChooserAspectJ() throws IOException {
            try {
                FileSystemAccessPenguin.accessFileSystemViaFileSystemProvider();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }

        @TestTest
        @PublicTest
        @Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/OnePathAllowedInstrumentationDelete.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/student")
        void test_accessFileSystemViaJFileChooserInstrumentation() {
            try {
                FileSystemAccessPenguin.accessFileSystemViaJFileChooser();
                fail(errorMessage);
            } catch (SecurityException e) {
                // Expected exception
            }
        }
        //</editor-fold>
    }
    //</editor-fold>
}
