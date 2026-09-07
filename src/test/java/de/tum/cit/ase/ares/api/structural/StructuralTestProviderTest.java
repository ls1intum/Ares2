package de.tum.cit.ase.ares.api.structural;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;

/**
 * Regression tests for
 * {@link StructuralTestProvider#checkParameters(Class[], com.fasterxml.jackson.databind.JsonNode, boolean)},
 * guarding against I-099: parameters were previously compared using
 * {@link Class#getSimpleName()} only, so a canonical oracle entry such as
 * {@code java.lang.String} could never match, even though
 * {@link StructuralTestProvider#checkExpectedType(Class, java.lang.reflect.Type, String)}
 * (used for return/field/annotation types) already accepted either form.
 */
class StructuralTestProviderTest {

	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	private static ArrayNode expectedParameterNames(String... names) {
		var array = OBJECT_MAPPER.createArrayNode();
		for (String name : names) {
			array.add(name);
		}
		return array;
	}

	@Test
	void strictOrderAcceptsCanonicalName() {
		var observed = new Class<?>[] { String.class, int.class };
		var expected = expectedParameterNames("java.lang.String", "int");
		assertThat(StructuralTestProvider.checkParameters(observed, expected, true)).isTrue();
	}

	@Test
	void strictOrderStillRejectsAMismatchedType() {
		var observed = new Class<?>[] { String.class, int.class };
		var expected = expectedParameterNames("java.lang.Integer", "int");
		assertThat(StructuralTestProvider.checkParameters(observed, expected, true)).isFalse();
	}

	@Test
	void unorderedMatchAcceptsCanonicalNameRegardlessOfPosition() {
		var observed = new Class<?>[] { String.class, int.class };
		var expected = expectedParameterNames("int", "java.lang.String");
		assertThat(StructuralTestProvider.checkParameters(observed, expected, false)).isTrue();
	}

	@Test
	void unorderedMatchStillRejectsAWrongDuplicateType() {
		var observed = new Class<?>[] { int.class, int.class };
		var expected = expectedParameterNames("int", "java.lang.String");
		assertThat(StructuralTestProvider.checkParameters(observed, expected, false)).isFalse();
	}

	@Test
	void unorderedMatchAcceptsRepeatedType() {
		var observed = new Class<?>[] { int.class, int.class };
		var expected = expectedParameterNames("int", "int");
		assertThat(StructuralTestProvider.checkParameters(observed, expected, false)).isTrue();
	}

	@Test
	void mismatchedParameterCountIsRejected() {
		var observed = new Class<?>[] { String.class };
		var expected = expectedParameterNames("java.lang.String", "int");
		assertThat(StructuralTestProvider.checkParameters(observed, expected, false)).isFalse();
	}

	@Test
	void noParametersMatch() {
		var observed = new Class<?>[0];
		var expected = expectedParameterNames();
		assertThat(StructuralTestProvider.checkParameters(observed, expected, false)).isTrue();
	}
}
