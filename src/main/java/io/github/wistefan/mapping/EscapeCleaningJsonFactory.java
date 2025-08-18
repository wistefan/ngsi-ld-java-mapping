package io.github.wistefan.mapping;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import io.micronaut.context.annotation.Primary;
import jakarta.inject.Singleton;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.Reader;

@Primary
@Singleton
public class EscapeCleaningJsonFactory extends JsonFactory {

	private final JsonFactory delegate = new JsonFactory();

	public EscapeCleaningJsonFactory() {
		// Copy config from the default if needed (modules are applied later via ObjectMapper)
		this.setCodec(delegate.getCodec());
	}

	@Override
	public JsonParser createParser(String content) throws IOException {
		return new EscapeCleaningParser(delegate.createParser(content));
	}

	@Override
	public JsonParser createParser(byte[] data) throws IOException {
		return new EscapeCleaningParser(delegate.createParser(data));
	}

	@Override
	public JsonParser createParser(InputStream in) throws IOException {
		return new EscapeCleaningParser(delegate.createParser(in));
	}

	@Override
	public JsonParser createParser(File f) throws IOException {
		return new EscapeCleaningParser(delegate.createParser(f));
	}

	@Override
	public JsonParser createParser(Reader r) throws IOException {
		return new EscapeCleaningParser(delegate.createParser(r));
	}

	@Override
	public JsonParser createParser(byte[] data, int offset, int len) throws IOException {
		return new EscapeCleaningParser(delegate.createParser(data, offset, len));
	}

	@Override
	public JsonParser createParser(char[] data) throws IOException {
		return new EscapeCleaningParser(delegate.createParser(data));
	}

	@Override
	public JsonParser createParser(char[] data, int offset, int len) throws IOException {
		return new EscapeCleaningParser(delegate.createParser(data, offset, len));
	}

	@Override
	public String getFormatName() {
		return FORMAT_NAME_JSON;
	}
}
