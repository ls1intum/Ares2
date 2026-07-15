package de.tum.cit.ase.ares.api.architecture.java.wala.fixture;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;

/** Test fixture whose directly reachable dependency is packaged in a JAR. */
public final class JarDependencyUser {
	private JarDependencyUser() {
	}

	public static void deleteFile(File file) throws IOException {
		FileUtils.forceDelete(file);
	}
}
