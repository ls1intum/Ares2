package de.tum.cit.ase.ares.api.structural;

import static de.tum.cit.ase.ares.api.localization.Messages.localized;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertThrows;

import org.junit.jupiter.api.Test;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.JsonNodeType;

/**
 * Checks that a broken structure oracle, the file in which the instructor
 * describes the expected classes, is refused with the Ares error template.
 */
class StructuralOracleRefusalTest {

	/** Reads the small oracle fragments the tests use. */
	private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

	/** Text that is not JSON is refused, and the parser's complaint is kept. */
	@Test
	void refusesTextThatIsNotJson() {
		SecurityException refused = assertThrows(SecurityException.class,
				() -> StructuralTestProvider.parseJsonArray("{"));
		assertEquals(localized("security.structural.oracle.json.invalid"), refused.getMessage());
		assertInstanceOf(JsonProcessingException.class, refused.getCause());
	}

	/**
	 * A property that has to be an array is refused when it holds something else.
	 *
	 * @throws JsonProcessingException never, the fragment is valid JSON
	 */
	@Test
	void refusesAPropertyThatIsNotAnArray() throws JsonProcessingException {
		JsonNode element = OBJECT_MAPPER.readTree("{\"methods\": 1}");
		SecurityException refused = assertThrows(SecurityException.class,
				() -> StructuralTestProvider.getExpectedJsonProperty(element, "methods"));
		assertEquals(localized("security.structural.oracle.property.not.array", "methods", JsonNodeType.NUMBER),
				refused.getMessage());
	}

	/**
	 * A property that has to be a boolean is refused when it holds something else.
	 *
	 * @throws JsonProcessingException never, the fragment is valid JSON
	 */
	@Test
	void refusesAPropertyThatIsNotABoolean() throws JsonProcessingException {
		JsonNode element = OBJECT_MAPPER.readTree("{\"isInterface\": \"yes\"}");
		SecurityException refused = assertThrows(SecurityException.class,
				() -> StructuralTestProvider.getExpectedJsonBooleanProperty(element, "isInterface"));
		assertEquals(localized("security.structural.oracle.property.not.boolean", "isInterface", JsonNodeType.STRING),
				refused.getMessage());
	}
}
