package de.tum.cit.ase.ares.api.structural;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * Checks that the constructors of a non-static nested class are compared with
 * the parameters written in its source, without the hidden first parameter Java
 * adds for the enclosing object.
 */
class ConstructorTestProviderTest {

	/** Reads the expected constructors written as JSON. */
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** The enclosing class of the nested classes below. */
	static class Outer {
		/** A non-static nested class, whose constructors get the hidden parameter. */
		class Inner {
			/**
			 * A constructor without parameters in the source.
			 */
			protected Inner() {
			}

			/**
			 * A constructor with one parameter in the source.
			 *
			 * @param name ignored
			 */
			protected Inner(String name) {
			}

			/**
			 * A constructor whose source parameter has the enclosing class's type.
			 *
			 * @param other ignored
			 */
			protected Inner(Outer other) {
			}
		}

		/** A static nested class, whose constructors get no hidden parameter. */
		static class StaticNested {
			/**
			 * A constructor whose source parameter has the enclosing class's type.
			 *
			 * @param other ignored
			 */
			protected StaticNested(Outer other) {
			}
		}
	}

	/**
	 * Reads the expected constructors from JSON.
	 *
	 * @param json the constructors as written in {@code test.json}
	 * @return the parsed constructors
	 * @throws Exception if the JSON is malformed
	 */
	private static JsonNode constructors(String json) throws Exception {
		return OBJECT_MAPPER.readTree(json);
	}

	/**
	 * Both source constructors of a non-static nested class match, the one without
	 * parameters and the one with a {@code String}.
	 *
	 * @throws Exception if the JSON is malformed
	 */
	@Test
	void nonStaticNestedClassConstructorsMatchTheirSourceParameters() throws Exception {
		var expected = constructors("""
				[ { "modifiers": [ "protected" ] },
				  { "modifiers": [ "protected" ], "parameters": [ "java.lang.String" ] } ]
				""");
		assertThatCode(() -> ConstructorTestProvider.checkConstructors("Outer.Inner", Outer.Inner.class, expected))
				.doesNotThrowAnyException();
	}

	/**
	 * A source parameter of the enclosing class's type is kept, both on a
	 * non-static and on a static nested class.
	 *
	 * @throws Exception if the JSON is malformed
	 */
	@Test
	void sourceParameterOfTheEnclosingTypeIsKept() throws Exception {
		var expected = constructors("""
				[ { "modifiers": [ "protected" ], "parameters": [ "Outer" ] } ]
				""");
		assertThatCode(() -> ConstructorTestProvider.checkConstructors("Outer.Inner", Outer.Inner.class, expected))
				.doesNotThrowAnyException();
		assertThatCode(() -> ConstructorTestProvider.checkConstructors("Outer.StaticNested", Outer.StaticNested.class,
				expected)).doesNotThrowAnyException();
	}

	/**
	 * A parameter list the source does not declare is still rejected.
	 *
	 * @throws Exception if the JSON is malformed
	 */
	@Test
	void nonStaticNestedClassStillRejectsAWrongParameter() throws Exception {
		var expected = constructors("""
				[ { "modifiers": [ "protected" ], "parameters": [ "int" ] } ]
				""");
		assertThatThrownBy(() -> ConstructorTestProvider.checkConstructors("Outer.Inner", Outer.Inner.class, expected))
				.isInstanceOf(AssertionError.class);
	}
}
