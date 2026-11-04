package io.github.wistefan.mapping;

import com.sun.net.httpserver.HttpServer;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class HttpJsonLdContextLoaderTest {

	private static final String CONTEXT_BODY = "{\"@context\":{\"name\":\"https://my.org/vocab/name\"}}";
	private static final String CONTEXT_PATH = "/context.jsonld";
	private static final String MOVED_PATH = "/moved.jsonld";
	private static final String MISSING_PATH = "/missing.jsonld";
	private static final String LOCATION_HEADER = "Location";
	private static final int HTTP_OK = 200;
	private static final int HTTP_MOVED_PERMANENTLY = 301;
	private static final int HTTP_NOT_FOUND = 404;
	private static final int ANY_FREE_PORT = 0;
	private static final int NO_BACKLOG = 0;
	private static final int IMMEDIATE_SHUTDOWN = 0;

	private HttpServer httpServer;
	private HttpJsonLdContextLoader loader;

	@BeforeEach
	void setUp() throws IOException {
		httpServer = HttpServer.create(new InetSocketAddress(ANY_FREE_PORT), NO_BACKLOG);
		httpServer.createContext(CONTEXT_PATH, exchange -> {
			byte[] body = CONTEXT_BODY.getBytes(StandardCharsets.UTF_8);
			exchange.sendResponseHeaders(HTTP_OK, body.length);
			exchange.getResponseBody().write(body);
			exchange.close();
		});
		httpServer.createContext(MOVED_PATH, exchange -> {
			exchange.getResponseHeaders().add(LOCATION_HEADER, CONTEXT_PATH);
			exchange.sendResponseHeaders(HTTP_MOVED_PERMANENTLY, -1);
			exchange.close();
		});
		httpServer.createContext(MISSING_PATH, exchange -> {
			exchange.sendResponseHeaders(HTTP_NOT_FOUND, -1);
			exchange.close();
		});
		httpServer.start();

		MappingProperties mappingProperties = new MappingProperties();
		mappingProperties.setContextRetrievalTimeout(Duration.ofSeconds(5));
		loader = new HttpJsonLdContextLoader(mappingProperties);
	}

	@AfterEach
	void tearDown() {
		httpServer.stop(IMMEDIATE_SHUTDOWN);
	}

	@Test
	void retrievesTheDocument() {
		assertEquals(Optional.of(CONTEXT_BODY), loader.load(urlOf(CONTEXT_PATH)), "The document should be returned.");
	}

	@Test
	void followsRedirects() {
		assertEquals(Optional.of(CONTEXT_BODY), loader.load(urlOf(MOVED_PATH)), "The redirect should be followed.");
	}

	@Test
	void reportsAnErrorStatusAsEmpty() {
		assertTrue(loader.load(urlOf(MISSING_PATH)).isEmpty(), "A 404 should not fail the mapping.");
	}

	@Test
	void reportsAnUnreachableServerAsEmpty() {
		String unreachable = urlOf(CONTEXT_PATH);
		httpServer.stop(IMMEDIATE_SHUTDOWN);

		assertTrue(loader.load(unreachable).isEmpty(), "An unreachable context should not fail the mapping.");
	}

	@Test
	void reportsAnInvalidUrlAsEmpty() {
		assertTrue(loader.load("not a url").isEmpty(), "An invalid url should not fail the mapping.");
	}

	private String urlOf(String path) {
		return "http://localhost:" + httpServer.getAddress().getPort() + path;
	}

}
