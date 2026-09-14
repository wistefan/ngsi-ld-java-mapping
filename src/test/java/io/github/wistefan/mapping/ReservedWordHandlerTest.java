package io.github.wistefan.mapping;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class ReservedWordHandlerTest {

	@Test
	public void test() {
		assertEquals("@context", ReservedWordHandler.removeEscape("tmfEscaped-@context"));
	}

}