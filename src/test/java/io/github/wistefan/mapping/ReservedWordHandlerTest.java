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

	@ParameterizedTest
	@ValueSource(strings = {"@referredType", "@baseType", "@schemaLocation", "@Referred", "@", "@1"})
	void atKeysOfUnmappedPropertiesAreEscaped(String key) {
		String escaped = ReservedWordHandler.escapeUnmappedKey(key);
		assertEquals("tmfEscaped-" + key, escaped, "An unmapped key starting with @ should be escaped.");
		assertTrue(ReservedWordHandler.canUnescapeDuringParsing(escaped),
				"The escaped key does not collide with a VO field, so the parser may unescape it.");
		assertEquals(key, ReservedWordHandler.removeEscape(escaped), "The escape should be reverted on read.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"@id", "@type", "@value", "@context", "id", "value", "datasetId"})
	void reservedWordsOfUnmappedPropertiesKeepTheirEscaping(String reservedWord) {
		assertEquals(ReservedWordHandler.escapeReservedWords(reservedWord), ReservedWordHandler.escapeUnmappedKey(reservedWord),
				"Reserved words should be escaped as before.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"attachment", "attribute", "atReferredType", "atType", "name", "role", "a@b"})
	void unmappedKeysNotStartingWithAtAreNotChanged(String key) {
		assertEquals(key, ReservedWordHandler.escapeUnmappedKey(key), "The key should be written unchanged.");
	}

	@ParameterizedTest
	@ValueSource(strings = {"@referredType", "@baseType"})
	void rawAtKeysStoredBeforeAreReadUnchanged(String legacyKey) {
		assertEquals(legacyKey, ReservedWordHandler.removeEscape(legacyKey),
				"Keys stored with a raw @ (old data) should be read as they are.");
	}

	@Test
	void typedEscapingIsNotChanged() {
		assertEquals("@referredType", ReservedWordHandler.escapeReservedWords("@referredType"),
				"escapeReservedWords is used for typed attributes and must not change.");
	}

}
