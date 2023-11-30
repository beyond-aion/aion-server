package com.aionemu.gameserver.utils.xml;

import java.io.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ForkJoinPool;
import java.util.concurrent.Future;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

/**
 * @author ?, Neon
 */
public class JAXBUtil {

	private static final Map<Class<?>, Future<JAXBContext>> CONTEXTS = new ConcurrentHashMap<>();

	public static void preLoadContextAsync(Class<?> clazz) {
		CONTEXTS.put(clazz, ForkJoinPool.commonPool().submit(() -> JAXBContext.newInstance(clazz)));
	}

	public static String serialize(Object obj) {
		return serialize(obj, null);
	}

	public static String serialize(Object obj, String schemaFile) {
		try {
			JAXBContext jc = JAXBContext.newInstance(obj.getClass());
			Marshaller m = jc.createMarshaller();
			if (schemaFile != null)
				m.setSchema(XmlUtil.getSchema(schemaFile));
			m.setProperty(Marshaller.JAXB_ENCODING, "UTF-8");
			m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);
			StringWriter sw = new StringWriter();
			m.marshal(obj, sw);
			return sw.toString();
		} catch (JAXBException e) {
			throw new RuntimeException("Failed to marshal object of class " + obj.getClass().getName(), e);
		}
	}

	public static Document serializeToDocument(Object obj) {
		String s = serialize(obj);
		return XmlUtil.getDocument(s);
	}

	public static <T> T deserialize(String xmlCode, Class<T> clazz) {
		return deserialize(xmlCode, clazz, (Schema) null);
	}

	public static <T> T deserialize(String xmlCode, Class<T> clazz, String schemaCode) {
		return deserialize(xmlCode, clazz, XmlUtil.getSchema(schemaCode, true));
	}

	/**
	 * Note: The unmarshaller automatically closes the reader
	 */
	public static <T> T deserialize(Reader reader, Class<T> clazz) throws IOException {
		return deserialize(reader, clazz, null);
	}

	public static <T> T deserialize(File file, Class<T> clazz) {
		return deserialize(file, clazz, (Schema) null);
	}

	public static <T> T deserialize(File file, Class<T> clazz, String schemaFile) {
		return deserialize(file, clazz, XmlUtil.getSchema(schemaFile));
	}

	public static <T> T deserialize(Document doc, Class<T> clazz, String schemaFile) {
		return deserialize(doc, clazz, XmlUtil.getSchema(schemaFile));
	}

	@SuppressWarnings("unchecked")
	private static <T> T deserialize(Object xml, Class<T> clazz, Schema schema) {
		Unmarshaller u = newUnmarshaller(clazz, schema);
		try {
			return switch (xml) {
				case Reader reader -> (T) u.unmarshal(reader);
				case File file -> (T) u.unmarshal(file);
				case Node node -> u.unmarshal(node, clazz).getValue();
				default -> (T) u.unmarshal(new StringReader(xml.toString()));
			};
		} catch (Exception e) {
			throw new RuntimeException("Failed to unmarshal class " + clazz.getName() + " from " + xml, e);
		}
	}

	@SuppressWarnings("unchecked")
	public static <T> List<T> deserialize(Collection<File> files, Class<T> clazz, String schemaFile) {
		Unmarshaller u = newUnmarshaller(clazz, XmlUtil.getSchema(schemaFile));
		List<T> objects = new ArrayList<>();
		for (File file : files) {
			try {
				objects.add((T) u.unmarshal(file));
			} catch (Exception e) {
				throw new RuntimeException("Failed to unmarshal class " + clazz.getName() + " from file:\n" + file, e);
			}
		}
		return objects;
	}

	public static Unmarshaller newUnmarshaller(Class<?> clazz, Schema schema) {
		Future<JAXBContext> contextTask = CONTEXTS.remove(clazz);
		try {
			JAXBContext jc = contextTask == null ? JAXBContext.newInstance(clazz) : contextTask.get();
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new XmlValidationHandler());
			u.setSchema(schema);
			return u;
		} catch (InterruptedException | ExecutionException e) {
			throw new RuntimeException(e instanceof ExecutionException ? e.getCause() : e);
		} catch (JAXBException e) {
			ByteArrayOutputStream os = new ByteArrayOutputStream();
			try (PrintStream ps = new PrintStream(os)) {
				e.printStackTrace(ps); // we need to do this, to get detailed information about what caused the issue
				throw new RuntimeException("Failed to initialize unmarshaller for class " + clazz.getName() + "\n" + os);
			}
		}
	}

	public static String generateSchema(Class<?>... classes) {
		try {
			JAXBContext jc = JAXBContext.newInstance(classes);
			StringSchemaOutputResolver ssor = new StringSchemaOutputResolver();
			jc.generateSchema(ssor);
			return ssor.getSchemma();
		} catch (Exception e) {
			throw new RuntimeException("Failed to generate schema", e);
		}
	}
}
