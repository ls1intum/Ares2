package de.tum.cit.ase.ares.api.architecture.java.wala.fixture;

import java.io.IOException;
import java.nio.file.Path;

import anonymous.sibling.SiblingFileHelper;

/**
 * Entry-point fixture whose forbidden call is in a sibling compiled package.
 */
public final class SiblingDependencyUser {
	private SiblingDependencyUser() {
	}

	public static String read(Path path) throws IOException {
		return SiblingFileHelper.read(path);
	}
}
