package io.github.wistefan.mapping;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.junit.jupiter.api.Assertions.*;

class ReservedWordHandlerTest {

	@Test
	public void test() {
		assertEquals("@context", ReservedWordHandler.removeEscape("tmfEscaped-@context"));
	}

	@ParameterizedTest
	@ValueSource(strings = {"datasetId", "expiresAt", "ngsildproof", "observedAt", "unitCode", "valueType",
			"createdAt", "modifiedAt", "deletedAt", "instanceId"})
	void newlyAddedReservedWordsAreEscaped(String reservedWord) {
		assertEquals("tmfEscaped-" + reservedWord, ReservedWordHandler.escapeReservedWords(reservedWord),
				"The newly added reserved word should be escaped.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"datasetId", "observedAt", "unitCode", "createdAt", "modifiedAt", "instanceId"})
	void newlyAddedVoFieldCollisionsCannotBeUnescapedDuringParsing(String collidingWord) {
		assertFalse(ReservedWordHandler.canUnescapeDuringParsing("tmfEscaped-" + collidingWord),
				"A word colliding with an explicit VO setter must keep its escape prefix until EntityVOMapper resolves it.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"expiresAt", "ngsildproof", "valueType", "deletedAt"})
	void newlyAddedNonCollidingReservedWordsCanBeUnescapedDuringParsing(String nonCollidingWord) {
		assertTrue(ReservedWordHandler.canUnescapeDuringParsing("tmfEscaped-" + nonCollidingWord),
				"A reserved word with no explicit VO setter may be safely unescaped during parsing.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"datasetId", "expiresAt", "ngsildproof", "observedAt", "unitCode", "valueType",
			"createdAt", "modifiedAt", "deletedAt", "instanceId"})
	void newlyAddedReservedWordsSurviveEscapeUnescapeRoundTrip(String reservedWord) {
		String escaped = ReservedWordHandler.escapeReservedWords(reservedWord);
		assertTrue(ReservedWordHandler.isReservedProperty(escaped));
		assertEquals(reservedWord, ReservedWordHandler.removeEscape(escaped));
	}

}
