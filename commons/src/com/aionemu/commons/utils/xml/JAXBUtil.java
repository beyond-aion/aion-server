package com.aionemu.commons.utils.xml;

import org.w3c.dom.Document;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;
import java.io.StringReader;
import java.io.StringWriter;
import java.net.URL;

public class JAXBUtil {

	public static String serialize(Object obj) {
		try {
			JAXBContext jc = JAXBContext.newInstance(obj.getClass());
			Marshaller m = jc.createMarshaller();
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter sw = new StringWriter();
			m.marshal(obj, sw);
			return sw.toString();
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to marshall object of class " + obj.getClass().getName(), e);
		}
	}

	public static Document serializeToDocument(Object obj) {
		String s = serialize(obj);
		return XmlUtils.getDocument(s);
	}

	public static <T> T deserialize(String s, Class<T> clazz) {
		return deserialize(s, clazz, (Schema) null);
	}

	public static <T> T deserialize(String s, Class<T> clazz, URL schemaURL) {
		Schema schema = XmlUtils.getSchema(schemaURL);
		return deserialize(s, clazz, schema);
	}

	public static <T> T deserialize(String s, Class<T> clazz, String schemaString) {
		Schema schema = XmlUtils.getSchema(schemaString);
		return deserialize(s, clazz, schema);
	}

	public static <T> T deserialize(Document xml, Class<T> clazz, String schemaString) {
		String xmlAsString = XmlUtils.getString(xml);
		return deserialize(xmlAsString, clazz, schemaString);
	}

	@SuppressWarnings("unchecked")
	public static <T> T deserialize(String s, Class<T> clazz, Schema schema) {
		try {
			JAXBContext jc = JAXBContext.newInstance(clazz);
			Unmarshaller u = jc.createUnmarshaller();
			u.setSchema(schema);
			return (T) u.unmarshal(new StringReader(s));
		} catch (Exception e) {
			throw new RuntimeException("Failed to unmarshall class " + clazz.getName() + " from xml:\n " + s, e);
		}
	}

	public static String generateSchemma(Class<?>... classes) {
		try {
			JAXBContext jc = JAXBContext.newInstance(classes);
			StringSchemaOutputResolver ssor = new StringSchemaOutputResolver();
			jc.generateSchema(ssor);
			return ssor.getSchemma();
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate schemma", e);
		}
	}
}
