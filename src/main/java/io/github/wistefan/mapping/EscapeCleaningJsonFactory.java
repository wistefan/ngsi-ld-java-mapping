package io.github.wistefan.mapping;

import io.micronaut.context.annotation.Primary;
import io.micronaut.jackson.JacksonConfiguration;
import jakarta.inject.Inject;
import jakarta.inject.Singleton;
import tools.jackson.core.JsonParser;
import tools.jackson.core.ObjectReadContext;
import tools.jackson.core.io.IOContext;
import tools.jackson.core.json.JsonFactory;
import tools.jackson.core.json.JsonFactoryBuilder;

import java.io.DataInput;
import java.io.InputStream;
import java.io.Reader;

/**
 * JsonFactory whose parsers unescape reserved-word keys (see {@link EscapeCleaningParser}).
 * Replaces Micronaut's default factory, so the context mapper is built on it.
 */
@Primary
@Singleton
public class EscapeCleaningJsonFactory extends JsonFactory {

	public EscapeCleaningJsonFactory() {
		super();
	}

	/**
	 * Applies {@code jackson.json-factory-features}, as Micronaut's own factory bean does.
	 */
	@Inject
	public EscapeCleaningJsonFactory(JacksonConfiguration jacksonConfiguration) {
		this(configuredBuilder(jacksonConfiguration));
	}

	protected EscapeCleaningJsonFactory(JsonFactoryBuilder builder) {
		super(builder);
	}

	protected EscapeCleaningJsonFactory(JsonFactory src) {
		super(src);
	}

	private static JsonFactoryBuilder configuredBuilder(JacksonConfiguration jacksonConfiguration) {
		JsonFactoryBuilder builder = JsonFactory.builder();
		jacksonConfiguration.getJsonFactoryFeatures().forEach(builder::configure);
		return builder;
	}

	@Override
	public EscapeCleaningJsonFactory copy() {
		return new EscapeCleaningJsonFactory(this);
	}

	/**
	 * Not supported: {@link JsonFactoryBuilder} can only build a plain JsonFactory, which would drop escape cleaning.
	 */
	@Override
	public JsonFactoryBuilder rebuild() {
		throw new UnsupportedOperationException("EscapeCleaningJsonFactory cannot be rebuilt, use copy() instead.");
	}

	@Override
	protected Object readResolve() {
		return new EscapeCleaningJsonFactory(this);
	}

	@Override
	protected JsonParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt, InputStream in) {
		return new EscapeCleaningParser(super._createParser(readCtxt, ioCtxt, in));
	}

	@Override
	protected JsonParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt, Reader r) {
		return new EscapeCleaningParser(super._createParser(readCtxt, ioCtxt, r));
	}

	@Override
	protected JsonParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt, char[] data, int offset, int len,
			boolean recyclable) {
		return new EscapeCleaningParser(super._createParser(readCtxt, ioCtxt, data, offset, len, recyclable));
	}

	@Override
	protected JsonParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt, byte[] data, int offset, int len) {
		return new EscapeCleaningParser(super._createParser(readCtxt, ioCtxt, data, offset, len));
	}

	@Override
	protected JsonParser _createParser(ObjectReadContext readCtxt, IOContext ioCtxt, DataInput input) {
		return new EscapeCleaningParser(super._createParser(readCtxt, ioCtxt, input));
	}
}
