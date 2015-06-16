package com.aionemu.commons.utils.xml;

import javax.xml.bind.SchemaOutputResolver;
import javax.xml.transform.Result;
import javax.xml.transform.stream.StreamResult;
import java.io.IOException;
import java.io.StringWriter;

public class StringSchemaOutputResolver extends SchemaOutputResolver {

	private StringWriter sw = null;

	@Override
	public Result createOutput(String namespaceUri, String suggestedFileName) throws IOException {
		sw = new StringWriter();
		StreamResult sr = new StreamResult();

		// If it's not set - schemagen throws AssertionError
		sr.setSystemId(String.valueOf(System.currentTimeMillis()));

		sr.setWriter(sw);
		return sr;
	}

	public String getSchemma() {
		return sw != null ? sw.toString() : null;
	}
}
