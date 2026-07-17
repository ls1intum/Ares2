package de.tum.cit.ase.ares.api.architecture.java.wala.fixture;

import java.io.IOException;
import java.io.InputStream;

import javax.activation.FileDataSource;

/** Test fixture whose javax.* dependency is supplied by a third-party JAR. */
public final class JavaxJarDependencyUser {
	private JavaxJarDependencyUser() {
	}

	public static InputStream openFile(String path) throws IOException {
		return new FileDataSource(path).getInputStream();
	}
}
