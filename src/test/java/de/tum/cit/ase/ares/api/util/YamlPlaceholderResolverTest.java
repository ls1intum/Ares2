package de.tum.cit.ase.ares.api.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

class YamlPlaceholderResolverTest {

	@Test
	void expandsProjectRootPlaceholder() {
		String userDir = System.getProperty("user.dir");
		String resolved = YamlPlaceholderResolver.expand("path: ${PROJECT_ROOT}/build/classes");
		assertThat(resolved).isEqualTo("path: " + userDir + "/build/classes");
	}

	@Test
	void expandsJavaHomePlaceholder() {
		String javaHome = System.getProperty("java.home");
		String resolved = YamlPlaceholderResolver.expand("jdk: ${java.home}");
		assertThat(resolved).isEqualTo("jdk: " + javaHome);
	}

	@Test
	void expandsUserHomePlaceholder() {
		String userHome = System.getProperty("user.home");
		String resolved = YamlPlaceholderResolver.expand("cache: ${user.home}/.gradle/jdks");
		assertThat(resolved).isEqualTo("cache: " + userHome + "/.gradle/jdks");
	}

	@Test
	void expandsMultipleOccurrencesOfTheSamePlaceholder() {
		String userDir = System.getProperty("user.dir");
		String resolved = YamlPlaceholderResolver.expand("a: ${PROJECT_ROOT}/x\nb: ${PROJECT_ROOT}/y");
		assertThat(resolved).isEqualTo("a: " + userDir + "/x\nb: " + userDir + "/y");
	}

	@Test
	void expandsAllPlaceholdersInTheSameContent() {
		String userDir = System.getProperty("user.dir");
		String javaHome = System.getProperty("java.home");
		String userHome = System.getProperty("user.home");
		String resolved = YamlPlaceholderResolver.expand("p: ${PROJECT_ROOT}\nj: ${java.home}\nu: ${user.home}");
		assertThat(resolved).isEqualTo("p: " + userDir + "\nj: " + javaHome + "\nu: " + userHome);
	}

	@Test
	void leavesUnknownPlaceholdersUntouched() {
		String resolved = YamlPlaceholderResolver.expand("path: ${UNKNOWN}/data");
		assertThat(resolved).isEqualTo("path: ${UNKNOWN}/data");
	}

	@Test
	void leavesContentWithoutPlaceholdersUnchanged() {
		String resolved = YamlPlaceholderResolver.expand("regardingTimeouts:\n  - timeout: 3000");
		assertThat(resolved).isEqualTo("regardingTimeouts:\n  - timeout: 3000");
	}

	@Test
	void replacesWithEmptyStringWhenSystemPropertyIsUnset() {
		String original = System.getProperty("user.home");
		try {
			System.clearProperty("user.home");
			String resolved = YamlPlaceholderResolver.expand("home: ${user.home}");
			assertThat(resolved).isEqualTo("home: ");
		} finally {
			if (original != null) {
				System.setProperty("user.home", original);
			}
		}
	}
}
