package io.github.wistefan.mapping;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import tools.jackson.core.JsonParser;
import tools.jackson.core.JsonToken;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.json.JsonFactory;

import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertInstanceOf;

class EscapeCleaningJsonFactoryTest {

	private static final String ESCAPED_JSON = "{\"tmfEscaped-@id\":\"my-id\"}";
	private static final String UNESCAPED_KEY = "@id";

	private static Stream<Arguments> derivedFactories() {
		return Stream.of(
				Arguments.of("plain", new EscapeCleaningJsonFactory()),
				Arguments.of("copied", new EscapeCleaningJsonFactory().copy()),
				Arguments.of("rebuilt", new EscapeCleaningJsonFactory().rebuild().build()));
	}

	@DisplayName("Factories derived from the escape cleaning factory still clean the escaped keys.")
	@ParameterizedTest(name = "{0}")
	@MethodSource("derivedFactories")
	void testEscapeCleaningIsPreserved(String factoryType, JsonFactory jsonFactory) {
		try (JsonParser parser = jsonFactory.createParser(ObjectReadContext.empty(), ESCAPED_JSON)) {
			assertEquals(JsonToken.START_OBJECT, parser.nextToken(), "The object should be started.");
			assertEquals(UNESCAPED_KEY, parser.nextName(),
					String.format("The %s factory should have removed the escape prefix.", factoryType));
		}
	}

	@DisplayName("Rebuilding the factory returns an escape cleaning factory.")
	@Test
	void testRebuildKeepsTheFactoryType() {
		assertInstanceOf(EscapeCleaningJsonFactory.class, new EscapeCleaningJsonFactory().rebuild().build(),
				"Rebuilding should not fall back to a plain JsonFactory.");
	}
}
