package org.cmdbuild.utils;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PipedInputStream;
import java.io.PipedOutputStream;

import javax.activation.DataSource;

public class InputStreamDataSource implements DataSource {

	String contentType;
	String name;

	PipedInputStream inputStream;
	PipedOutputStream outputStream;

	public InputStreamDataSource(String name, String contentType) throws IOException {
		this.name = name;
		this.contentType = contentType;
		this.inputStream = new PipedInputStream();
		this.outputStream = new PipedOutputStream(inputStream);
	}

	public String getContentType() {
		return contentType;
	}

	public String getName() {
		return name;
	}

	public InputStream getInputStream() throws IOException {
		if (inputStream == null)
			throw new IOException();
		return inputStream;
	}

	public OutputStream getOutputStream() throws IOException {
		if (outputStream == null)
			throw new IOException();
		return outputStream;
	}
}
