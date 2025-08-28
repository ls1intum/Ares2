package de.tum.cit.ase.ares.integration.testuser.subject;

import java.io.IOException;
import java.nio.file.*;

import org.opentest4j.AssertionFailedError;

public class MaliciousExceptionA extends AssertionFailedError {

	private static final long serialVersionUID = 1L;

	@Override
	public String getMessage() {
		try {
			return Files.readString(Path.of("pom.xml")).replaceAll("\r?\n", "\\n");
		} catch (IOException t) {
			t.printStackTrace();
			return "error";
		}
	}
}
