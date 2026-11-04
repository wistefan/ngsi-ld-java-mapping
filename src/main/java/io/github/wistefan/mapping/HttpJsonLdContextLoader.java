package io.github.wistefan.mapping;

import io.micronaut.context.annotation.Secondary;
import jakarta.inject.Singleton;
import lombok.extern.slf4j.Slf4j;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Optional;

/**
 * Default {@link JsonLdContextLoader}, retrieving the context document over HTTP. It uses the JDK client instead of
 * the broker client, since the context is not necessarily served by the broker.
 */
@Slf4j
@Singleton
@Secondary
public class HttpJsonLdContextLoader implements JsonLdContextLoader {

	private static final String ACCEPT_HEADER = "Accept";
	private static final String ACCEPT_JSON_LD = "application/ld+json, application/json";
	private static final int HTTP_OK = 200;
	private static final int HTTP_MULTIPLE_CHOICES = 300;

	private final HttpClient httpClient;
	private final Duration requestTimeout;

	public HttpJsonLdContextLoader(MappingProperties mappingProperties) {
		this.requestTimeout = mappingProperties.getContextRetrievalTimeout();
		this.httpClient = HttpClient.newBuilder()
				.followRedirects(HttpClient.Redirect.NORMAL)
				.connectTimeout(requestTimeout)
				.build();
	}

	@Override
	public Optional<String> load(String url) {
		try {
			HttpRequest request = HttpRequest.newBuilder(URI.create(url))
					.header(ACCEPT_HEADER, ACCEPT_JSON_LD)
					.timeout(requestTimeout)
					.GET()
					.build();
			HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
			if (response.statusCode() < HTTP_OK || response.statusCode() >= HTTP_MULTIPLE_CHOICES) {
				log.warn("Context {} answered with status {}.", url, response.statusCode());
				return Optional.empty();
			}
			return Optional.ofNullable(response.body());
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
			log.warn("Interrupted while retrieving the context {}.", url);
			return Optional.empty();
		} catch (RuntimeException | java.io.IOException e) {
			log.warn("Was not able to retrieve the context {}.", url, e);
			return Optional.empty();
		}
	}

}
