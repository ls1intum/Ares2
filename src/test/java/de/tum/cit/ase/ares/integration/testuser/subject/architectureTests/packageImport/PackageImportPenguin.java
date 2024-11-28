package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.packageImport;

import ch.qos.logback.core.FileAppender;

import java.io.IOException;

public class PackageImportPenguin {

    void accessPathThroughThirdPartyPackage() throws IOException {
        FileAppender fileAppender = new FileAppender();
        fileAppender.openFile("path/to/file");
    }
}
