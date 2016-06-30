package com.aionemu.commons.utils.xml;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.io.StringWriter;
import java.nio.charset.Charset;
import java.util.Collection;
import java.util.List;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBException;
import javax.xml.bind.Marshaller;
import javax.xml.bind.Unmarshaller;
import javax.xml.validation.Schema;

import org.w3c.dom.Document;
import org.w3c.dom.Node;

import javolution.util.FastTable;

/**
 * @author ?
 * @reworked Neon
 */
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
		return XmlUtil.getDocument(s);
	}

	/**
	 * @see #deserialize(String, Class, String)
	 */
	public static <T> T deserialize(String xml, Class<T> clazz) {
		return deserialize(xml, clazz, (Schema) null);
	}

	/**
	 * Unmarshals a string to an instance of the given class.
	 * 
	 * @param xml
	 *          - Plain XML code
	 * @param clazz
	 *          - Class which will hold the data
	 * @param clazz
	 *          - Schema code
	 * @return
	 */
	public static <T> T deserialize(String xml, Class<T> clazz, String schemaString) {
		return deserialize(xml, clazz, XmlUtil.getSchema(schemaString, true));
	}

	/**
	 * @see #deserialize(File, Class, String)
	 */
	public static <T> T deserialize(File file, Class<T> clazz) {
		return deserialize(file, clazz, (Schema) null);
	}

	/**
	 * Unmarshals a file to an instance of the given class.
	 * 
	 * @param file
	 *          - Input file
	 * @param clazz
	 *          - Class which will hold the data
	 * @param schemaFile
	 *          - Path to the schema file
	 * @return
	 */
	public static <T> T deserialize(File file, Class<T> clazz, String schemaFile) {
		return deserialize(file, clazz, XmlUtil.getSchema(schemaFile));
	}

	/**
	 * Unmarshals a document to an instance of the given class.
	 * 
	 * @param doc
	 *          - Input document
	 * @param clazz
	 *          - Class which will hold the data
	 * @param schemaFile
	 *          - Path to the schema file
	 * @return
	 */
	public static <T> T deserialize(Document doc, Class<T> clazz, String schemaFile) {
		return deserialize(doc, clazz, XmlUtil.getSchema(schemaFile));
	}

	@SuppressWarnings("unchecked")
	private static <T> T deserialize(Object xml, Class<T> clazz, Schema schema) {
		Unmarshaller u = newUnmarshaller(clazz, schema);
		try {
			T obj;
			if (xml instanceof File) {
				try (InputStreamReader isr = new InputStreamReader(new FileInputStream((File) xml), Charset.forName("UTF-8"))) {
					obj = (T) u.unmarshal(isr);
				}
			} else if (xml instanceof Node) { // superinterface of document
				obj = (T) u.unmarshal((Node) xml);
			} else {
				try (StringReader sr = new StringReader(xml.toString())) {
					obj = (T) u.unmarshal(sr);
				}
			}
			if (obj == null)
				throw new NullPointerException("Xml input does not contain any content for " + clazz.getName());
			return obj;
		} catch (Exception e) {
			throw new RuntimeException("Failed to unmarshal class " + clazz.getName() + " from xml:\n" + xml + "\nCause: " + e.getMessage());
		}
	}

	/**
	 * Unmarshals a collection of files to instances of the given class.
	 * 
	 * @param file
	 *          - Input files
	 * @param clazz
	 *          - Class which will hold the data
	 * @param schemaFile
	 *          - Path to the schema file
	 * @return List of created objects
	 */
	@SuppressWarnings("unchecked")
	public static <T> List<T> deserialize(Collection<File> files, Class<T> clazz, String schemaFile) {
		Unmarshaller u = newUnmarshaller(clazz, XmlUtil.getSchema(schemaFile));
		List<T> objects = new FastTable<>();
		for (File file : files) {
			try {
				T obj;
				try (InputStreamReader isr = new InputStreamReader(new FileInputStream(file), Charset.forName("UTF-8"))) {
					obj = (T) u.unmarshal(isr);
				}
				if (obj == null)
					throw new NullPointerException("File " + file + " does not contain any content for " + clazz.getName());
				objects.add(obj);
			} catch (Exception e) {
				throw new RuntimeException("Failed to unmarshal class " + clazz.getName() + " from file:\n" + file + "\nCause: " + e.getMessage());
			}
		}
		return objects;
	}

	public static Unmarshaller newUnmarshaller(Class<?> clazz, Schema schema) {
		try {
			JAXBContext jc = JAXBContext.newInstance(clazz);
			Unmarshaller u = jc.createUnmarshaller();
			u.setEventHandler(new XmlValidationHandler());
			u.setSchema(schema);
			return u;
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize unmarshaller for class " + clazz.getName(), e);
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
