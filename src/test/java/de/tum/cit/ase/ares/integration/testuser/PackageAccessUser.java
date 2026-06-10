package de.tum.cit.ase.ares.integration.testuser;

import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.MethodOrderer.MethodName;
import org.junit.jupiter.api.TestMethodOrder;

import de.tum.cit.ase.ares.api.*;
import de.tum.cit.ase.ares.api.MirrorOutput.MirrorOutputPolicy;
import de.tum.cit.ase.ares.api.jupiter.PublicTest;
import de.tum.cit.ase.ares.api.localization.UseLocale;
import de.tum.cit.ase.ares.integration.testuser.subject.PackageAccessPenguin;

@UseLocale("en")
@MirrorOutput(MirrorOutputPolicy.DISABLED)
@StrictTimeout(value = 300, unit = TimeUnit.MILLISECONDS)
@TestMethodOrder(MethodName.class)
@SuppressWarnings("static-method")
public class PackageAccessUser {

	@PublicTest
	void package_aBlacklistingRegex() {
		PackageAccessPenguin.usePattern();
	}

	@PublicTest
	void package_bBlacklistingJava() {
		PackageAccessPenguin.usePattern();
	}

	@PublicTest
	void package_cBlacklistingAll() {
		PackageAccessPenguin.usePattern();
	}

	@PublicTest
	void package_dBlackAndWhitelisting() {
		PackageAccessPenguin.usePattern();
	}

	@PublicTest
	void package_eBlackPenguinAgain() {
		PackageAccessPenguin.usePattern();
	}
}
