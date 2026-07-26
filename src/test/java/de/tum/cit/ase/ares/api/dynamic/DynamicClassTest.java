package de.tum.cit.ase.ares.api.dynamic;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;
import org.opentest4j.AssertionFailedError;

/**
 * Regression tests for
 * {@link DynamicClass#checkForPublicOrProtectedMethods(DynamicMethod...)},
 * guarding against I-094: the method previously exempted any public method
 * whose signature was a textual <em>suffix</em> of
 * {@code "main(java.lang.String[])"} (via {@code String.endsWith}) instead of
 * only the literal {@code main} method.
 */
class DynamicClassTest {

	static class MainSuffixFixture {
		public static void main(String[] args) {
			// intentionally empty: the literal main method must remain exempt
		}

		public void ain(String[] args) {
			// intentionally empty: a suffix of "main(java.lang.String[])" must NOT be
			// exempt
		}
	}

	@Test
	void publicMethodWhoseSignatureIsSuffixOfMainIsNotExempted() {
		var dynamicClass = DynamicClass.toDynamic(MainSuffixFixture.class);
		assertThatThrownBy(dynamicClass::checkForPublicOrProtectedMethods).isInstanceOf(AssertionFailedError.class)
				.hasMessageContaining("ain(java.lang.String[])");
	}

	static class OnlyLiteralMainFixture {
		public static void main(String[] args) {
			// intentionally empty: the only public method, must remain exempt
		}
	}

	@Test
	void literalMainMethodRemainsExempt() {
		var dynamicClass = DynamicClass.toDynamic(OnlyLiteralMainFixture.class);
		assertThat(dynamicClass.checkForPublicOrProtectedMethods()).isEqualTo(1);
	}
}
