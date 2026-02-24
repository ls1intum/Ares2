package de.tum.cit.ase.ares.integration.testuser.subject.architectureTests.packageImport;

import java.io.IOException;

import ch.qos.logback.core.FileAppender;

public class PackageImportPenguin {

	void accessPathThroughThirdPartyPackage() throws IOException {
		FileAppender<String> fileAppender = new FileAppender<>();
		fileAppender.openFile("path/to/file");
	}
}
