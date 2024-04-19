package de.tum.cit.ase.ares.integration.testuser.subject;

import java.util.regex.Pattern;

public final class PackageAccessPenguin {

	private PackageAccessPenguin() {
	}

	public static void usePattern() {
		Pattern pattern = Pattern.compile("a+");
		pattern.asMatchPredicate().test("aaa");
	}
}
