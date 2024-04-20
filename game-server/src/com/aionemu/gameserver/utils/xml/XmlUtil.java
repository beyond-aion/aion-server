package com.aionemu.gameserver.utils.xml;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Collection;
import java.util.stream.Collectors;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;

import org.w3c.dom.Document;
import org.xml.sax.InputSource;

/**
 * @author ?, Neon
 */
public abstract class XmlUtil {

	private static volatile DocumentBuilderFactory dbf;
	private static volatile TransformerFactory tf;

	public static Document getDocument(String xmlSource) {
		try {
			if (dbf == null)
				dbf = DocumentBuilderFactory.newInstance();
			dbf.setNamespaceAware(true);
			Reader stream = new StringReader(xmlSource);
			DocumentBuilder db = dbf.newDocumentBuilder();
			return db.parse(new InputSource(stream));
		} catch (Exception e) {
			throw new RuntimeException("Error converting string to document", e);
		}
	}

	public static String getString(Document document) {
		try {
			if (tf == null)
				tf = TransformerFactory.newInstance();
			DOMSource domSource = new DOMSource(document);
			StringWriter writer = new StringWriter();
			StreamResult result = new StreamResult(writer);
			Transformer transformer = tf.newTransformer();
			transformer.transform(domSource, result);
			return writer.toString();
		} catch (TransformerException e) {
			throw new RuntimeException("Error converting document to string", e);
		}
	}

	/**
	 * @param schemaFile
	 *          - Relative/absolute path to the schema file.
	 * @return {@link Schema} object representing xml schema of xml files
	 */
	public static Schema getSchema(String schemaFile) {
		return getSchema(schemaFile, false);
	}

	/**
	 * @param schemaFileOrSourceCode
	 *          - Relative/absolute file path to the schema file, or schema source code.
	 * @param isSourceCode
	 *          - Whether schemaUrlOrSourceCode is a file path or schema source code.
	 * @return {@link Schema} object representing xml schema of xml files
	 */
	public static Schema getSchema(String schemaFileOrSourceCode, boolean isSourceCode) {
		try {
			SchemaFactory sf = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
			if (isSourceCode)
				return sf.newSchema(new StreamSource(new StringReader(schemaFileOrSourceCode)));
			else
				return sf.newSchema(new File(schemaFileOrSourceCode));
		} catch (Exception e) {
			throw new RuntimeException("Failed to create schema from: " + schemaFileOrSourceCode, e);
		}
	}

	public static void validate(Schema schema, Document document) {
		Validator validator = schema.newValidator();
		try {
			validator.validate(new DOMSource(document));
		} catch (Exception e) {
			throw new RuntimeException("Failed to validate document", e);
		}
	}

	/**
	 * @see #listFiles(File, boolean)
	 */
	public static Collection<File> listFiles(String root, boolean recursive) {
		return listFiles(new File(root), recursive);
	}

	/**
	 * Searches for (non-hidden) .xml files and returns them in a list
	 * 
	 * @param root
	 *          - Absolute/relative path to the base directory
	 * @param recursive
	 *          - If set to true, include all subdirectories of the root directory
	 * @return List of .xml files inside the root directory.
	 */
	public static Collection<File> listFiles(File root, boolean recursive) {
		try {
			return Files.find(root.toPath(), recursive ? Integer.MAX_VALUE : 1, (path, attrs) -> attrs.isRegularFile() && path.toString().toLowerCase().endsWith(".xml")).map(Path::toFile).collect(Collectors.toList());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
}
