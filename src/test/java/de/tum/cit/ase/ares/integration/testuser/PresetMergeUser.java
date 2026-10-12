package de.tum.cit.ase.ares.integration.testuser;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.Policy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;

@UseLocale("en")
@TestMethodOrder(MethodName.class)
// Deliberately grants nothing itself for smoke-test-marker.txt: only
// basedOnTheFollowingPreset: SMOKE_TEST supplies that permission, proving the
// merge actually reaches real enforcement, not just the parsed object model.
@Policy(value = "src/test/resources/de/tum/cit/ase/ares/integration/testuser/securitypolicies/java/maven/archunit/aspectj/PolicyPresetMergeUser.yaml", withinPath = "test-classes/de/tum/cit/ase/ares/integration/testuser/subject/helloWorld")
@SuppressWarnings("static-method")
public class PresetMergeUser {

	@PublicTest
	void readsAFileOnlyThePresetGrants() throws IOException {
		Files.readString(Path.of(
				"src/test/java/de/tum/cit/ase/ares/integration/testuser/subject/presetMerge/smoke-test-marker.txt"));
	}

	@PublicTest
	void stillDeniesAFileNeitherSourceGrants() throws IOException {
		Files.readString(Path.of("pom.xml"));
	}
}
