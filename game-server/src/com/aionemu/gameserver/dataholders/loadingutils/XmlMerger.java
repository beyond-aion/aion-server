package com.aionemu.gameserver.dataholders.loadingutils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.SeekableByteChannel;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.util.Properties;
import java.util.zip.CRC32;

import javax.xml.namespace.QName;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.aionemu.commons.utils.PropertiesUtils;
import com.aionemu.gameserver.utils.xml.XmlUtil;

/**
 * <p>
 * <code>XmlMerger</code> is a utility that writes XML document onto an other document with resolving all <code>import</code> elements.
 * </p>
 * <p>
 * Schema:
 * 
 * <pre>
 * &lt;xs:element name="import"&gt;
 * &lt;xs:annotation&gt;
 * &lt;xs:documentation&gt;&lt;![CDATA[
 *      Attributes:
 *          'file' :
 *              Required attribute.
 *              Specified path to imported file or directory.
 *          'skipRoot' :
 *              Optional attribute.
 *              Default value: 'false'.
 *              If enabled, then root tags of imported files are ignored.
 *          'recirsiveImport':
 *              Optional attribute.
 *              Default value: 'true'.
 *              If enabled and attribute 'file' points to the directory, then all xml files in that
 *              directory ( and deeper - recursively ) will be imported, otherwise only files inside
 *              that directory (without it subdirectories)
 *  ]]&gt;&lt;/xs:documentation&gt;
 * &lt;/xs:annotation&gt;
 * &lt;xs:complexType&gt;
 * &lt;xs:attribute type="xs:string" name="file" use="required"/&gt;
 * &lt;xs:attribute type="xs:boolean" name="skipRoot" use="optional" default="false"/&gt;
 * &lt;xs:attribute type="xs:boolean" name="recursiveImport" use="optional" default="true" /&gt;
 * &lt;/xs:complexType&gt;
 * &lt;/xs:element&gt;
 * </pre>
 * </p>
 * <p/>
 * Created on: 23.07.2009 12:55:14
 * 
 * @author Aquanox, Neon
 */
public class XmlMerger {

	private static final Logger log = LoggerFactory.getLogger(XmlMerger.class);

	private static final QName qNameFile = new QName("file");

	/**
	 * If this option is enabled, the first found root tag will enclose all found files (which will then be written without their root tags).
	 */
	private static final QName qNameSingleRootTag = new QName("singleRootTag");

	/**
	 * If this option is enabled you import the directory, and all its subdirectories. Default is 'true'.
	 */
	private static final QName qNameRecursiveImport = new QName("recursiveImport");

	private final File baseDir;
	private final File sourceFile;
	private final File destFile;
	private final File metaDataFile;

	private XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

	/**
	 * Create new instance of <tt>XmlMerger </tt>. Base directory is set to directory which contains source file.
	 * 
	 * @param source
	 *          Source file.
	 * @param target
	 *          Destination file.
	 */
	public XmlMerger(File source, File target) {
		this(source, target, source.getParentFile());
	}

	/**
	 * Create new instance of <tt>XmlMerger </tt>
	 * 
	 * @param source
	 *          Source file.
	 * @param target
	 *          Destination file.
	 * @param baseDir
	 *          Root directory.
	 */
	public XmlMerger(File source, File target, File baseDir) {
		this.baseDir = baseDir;

		this.sourceFile = source;
		this.destFile = target;

		this.metaDataFile = new File(target.getParent(), target.getName() + ".properties");
	}

	/**
	 * This method creates a result document if it is missing, or updates existing one if the source file has modification.<br />
	 * If there are no changes - nothing happens.
	 * 
	 * @throws FileNotFoundException
	 *           when source file doesn't exists.
	 * @throws XMLStreamException
	 *           when XML processing error was occurred.
	 */
	public boolean process() throws Exception {
		log.debug("Processing " + sourceFile + " files into " + destFile);

		if (!sourceFile.exists())
			throw new FileNotFoundException("Source file " + sourceFile.getPath() + " not found.");

		if (!destFile.exists()) {
			log.info("Cache file not found. Updating...");
		} else if (!metaDataFile.exists()) {
			log.info("Meta file not found. Updating...");
		} else if (checkFileModifications()) {
			log.info("Modifications found. Updating...");
		} else {
			log.info("Cache file is up to date");
			return false;
		}

		doUpdate();
		return true;
	}

	/**
	 * Check for modifications of included files.
	 * 
	 * @return <code>true</code> if at least one of included files has modifications.
	 * @throws IOException
	 *           IO Error.
	 * @throws SAXException
	 *           Document parsing error.
	 * @throws ParserConfigurationException
	 *           if a SAX parser cannot be created which satisfies the requested configuration.
	 */
	private boolean checkFileModifications() throws Exception {
		if (sourceFile.lastModified() > destFile.lastModified()) {
			log.debug("Source file was modified ");
			return true;
		}

		Properties metadata = PropertiesUtils.load(metaDataFile, null);

		SAXParserFactory parserFactory = SAXParserFactory.newInstance();

		SAXParser parser = parserFactory.newSAXParser();

		TimeCheckerHandler handler = new TimeCheckerHandler(baseDir, metadata);

		parser.parse(sourceFile, handler);

		return handler.isModified();
	}

	/**
	 * This method processes the source file, replacing all of the 'import' tags by the data from the relevant files.
	 * 
	 * @throws XMLStreamException
	 *           on event writing error.
	 * @throws IOException
	 *           if the destination file exists but is a directory rather than a regular file, does not exist but cannot be created, or cannot be opened
	 *           for any other reason
	 */
	private void doUpdate() throws XMLStreamException, IOException {
		XMLStreamReader reader = null;
		XMLStreamWriter writer = null;

		Properties metadata = new Properties();

		try {
			writer = outputFactory.createXMLStreamWriter(new BufferedWriter(new FileWriter(destFile, false)));
			reader = inputFactory.createXMLStreamReader(new FileReader(sourceFile));

			writer.writeStartDocument();
			while (reader.hasNext()) {
				switch (reader.next()) {
					case XMLEvent.START_DOCUMENT: // skip start and end of document.
					case XMLEvent.END_DOCUMENT:
					case XMLEvent.COMMENT: // skip comments
					case XMLEvent.SPACE: // skip ignorable white-space
						continue;
					case XMLEvent.START_ELEMENT:
						if ("import".equals(reader.getLocalName())) { // import declared file
							processImportElement(reader, writer, metadata);
							continue;
						}
						break;
					case XMLEvent.END_ELEMENT: // skip closing import tag, since it's replaces by declared file contents
						if ("import".equals(reader.getLocalName()))
							continue;
				}

				if (reader.isWhiteSpace())// skip white-spaces
					continue;

				write(reader, writer);
			}
			writer.writeEndDocument();
			writer.flush();

			storeFileModifications(metadata, metaDataFile);
		} finally {
			if (writer != null)
				writer.close();
			if (reader != null)
				reader.close();
		}
	}

	/**
	 * This method processes the 'import' element, replacing it by the data from the relevant files.
	 * 
	 * @throws XMLStreamException
	 *           on event writing error.
	 * @throws FileNotFoundException
	 *           of imported file was not found.
	 */
	private void processImportElement(XMLStreamReader reader, XMLStreamWriter writer, Properties metadata) throws XMLStreamException, IOException {
		File file = new File(baseDir, getAttributeValue(reader, qNameFile, null));

		if (!file.exists())
			throw new FileNotFoundException("Missing file to import:" + file.getPath());

		QName startElement = null;
		if (file.isFile()) {
			importFile(file, false, false, writer, metadata);
		} else {
			boolean singleRootTag = Boolean.parseBoolean(getAttributeValue(reader, qNameSingleRootTag, "false"));
			boolean recImport = Boolean.parseBoolean(getAttributeValue(reader, qNameRecursiveImport, "true"));
			log.debug("Processing dir " + file);
			for (File file2 : XmlUtil.listFiles(file, recImport)) {
				boolean skipRootStartElement = singleRootTag && startElement != null;
				startElement = importFile(file2, skipRootStartElement, singleRootTag, writer, metadata);
			}
			writer.writeEndElement();
		}
	}

	/**
	 * Extract an attribute value from a <code>StartElement </code> event.
	 * 
	 * @param element
	 *          Event object.
	 * @param name
	 *          Attribute QName
	 * @param def
	 *          Default value.
	 * @param onErrorMessage
	 *          On error message.
	 * @return attribute value
	 * @throws XMLStreamException
	 *           if attribute is missing and there is no default value set.
	 */
	private String getAttributeValue(XMLStreamReader reader, QName name, String def) throws XMLStreamException {
		String attribute = reader.getAttributeValue(null, name.getLocalPart());

		if (attribute == null) {
			if (def == null)
				throw new XMLStreamException("Attribute '" + name.getLocalPart() + "' is missing or empty.", reader.getLocation());

			return def;
		}

		return attribute;
	}

	/**
	 * Read all {@link javax.xml.stream.events.XMLEvent}'s from specified file and write them onto the {@link javax.xml.stream.XMLStreamWriter}
	 * 
	 * @param file
	 *          File to import
	 * @param skipStartElement
	 *          If true, the start root tag will not be imported.
	 * @param skipEndElement
	 *          If true, the end root tag will not be imported.
	 * @param writer
	 *          Destination writer
	 * @param metadata
	 * @return The StartElement.
	 * @throws XMLStreamException
	 *           On event reading/writing error.
	 * @throws IOException
	 *           If the reading file does not exist, is a directory rather than a regular file, or for some other reason cannot be opened for reading.
	 */
	private QName importFile(File file, boolean skipStartElement, boolean skipEndElement, XMLStreamWriter writer, Properties metadata)
		throws XMLStreamException, IOException {
		log.debug("Appending file " + file);
		metadata.setProperty(file.getPath(), makeHash(file));

		XMLStreamReader reader = null;

		try {
			reader = inputFactory.createXMLStreamReader(new FileReader(file));

			QName startElement = null;

			while (reader.hasNext()) {
				switch (reader.next()) {
					case XMLEvent.START_DOCUMENT: // skip start and end of document.
					case XMLEvent.END_DOCUMENT:
					case XMLEvent.COMMENT: // skip comments
					case XMLEvent.SPACE: // skip ignorable white-space
						continue;
				}

				if (reader.isWhiteSpace()) // skip white-spaces
					continue;

				// modify root-tag of imported file.zzy
				if (startElement == null && reader.isStartElement()) {
					startElement = reader.getName();

					if (skipStartElement)
						continue;
				}

				if (skipEndElement && reader.isEndElement() && startElement != null && reader.getName().equals(startElement))
					continue;

				// finally - write tag
				write(reader, writer);
			}
			return startElement;
		} finally {
			if (reader != null)
				reader.close();
		}
	}

	private void write(XMLStreamReader reader, XMLStreamWriter writer) throws XMLStreamException {
		if (reader.isStartElement()) {
			if (reader.getNamespaceURI() == null)
				writer.writeStartElement(reader.getLocalName());
			else
				writer.writeStartElement(reader.getPrefix(), reader.getLocalName(), reader.getNamespaceURI());

			for (int i = 0, len = reader.getNamespaceCount(); i < len; i++)
				writer.writeNamespace(reader.getNamespacePrefix(i), reader.getNamespaceURI(i));
		} else if (reader.isEndElement()) {
			writer.writeEndElement();
		}
		if (reader.isStartElement() || reader.getEventType() == XMLEvent.ATTRIBUTE) {
			for (int i = 0, len = reader.getAttributeCount(); i < len; i++) {
				String attUri = reader.getAttributeNamespace(i);
				if (attUri != null)
					writer.writeAttribute(attUri, reader.getAttributeLocalName(i), reader.getAttributeValue(i));
				else
					writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
			}
		} else if (reader.isCharacters())
			writer.writeCharacters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
	}

	private static class TimeCheckerHandler extends DefaultHandler {

		private File basedir;
		private Properties metadata;

		private boolean isModified = false;

		private Locator locator;

		private TimeCheckerHandler(File basedir, Properties metadata) {
			this.basedir = basedir;
			this.metadata = metadata;
		}

		@Override
		public void setDocumentLocator(Locator locator) {
			this.locator = locator;
		}

		@Override
		public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {
			if (isModified || !"import".equals(qName))
				return;

			String value = attributes.getValue(qNameFile.getLocalPart());

			if (value == null)
				throw new SAXParseException("Attribute 'file' is missing", locator);

			File file = new File(basedir, value);

			if (!file.exists())
				throw new SAXParseException("Imported file not found. file=" + file.getPath(), locator);

			if (file.isFile() && checkFile(file)) { // if file - just check it.
				isModified = true;
				return;
			}

			if (file.isDirectory()) { // otherwise check all files inside
				String rec = attributes.getValue(qNameRecursiveImport.getLocalPart());
				for (File childFile : XmlUtil.listFiles(file, rec == null || Boolean.parseBoolean(rec))) {
					if (checkFile(childFile)) {
						isModified = true;
						return;
					}
				}
			}
		}

		private boolean checkFile(File file) {
			String data = metadata.getProperty(file.getPath());

			if (data == null) // file was added.
				return true;

			try {
				String hash = makeHash(file);

				if (!data.equals(hash))// file|dir was changed.
					return true;
			} catch (IOException e) {
				log.warn("File verification error. File: " + file.getPath() + ", location=" + locator.getLineNumber() + ":" + locator.getColumnNumber(), e);
				return true;// was modified.
			}

			return false;
		}

		public boolean isModified() {
			return isModified;
		}
	}

	private void storeFileModifications(Properties props, File file) throws IOException {
		try (FileWriter writer = new FileWriter(file, false)) {
			props.store(writer, " This file is machine-generated. DO NOT EDIT!");
		} catch (IOException e) {
			log.error("Failed to store file modification data.");
			throw e;
		}
	}

	/**
	 * Create a unique identifier of file and it contents.
	 * 
	 * @param file
	 *          the file to checksum, must not be <code>null</code>
	 * @return String identifier
	 * @throws IOException
	 *           if an IO error occurs reading the file
	 */
	private static String makeHash(File file) throws IOException {
		return String.valueOf(calculateCRC32(file.toPath()));
	}

	private static long calculateCRC32(Path filePath) throws IOException {
		CRC32 crc = new CRC32();
		ByteBuffer buffer = ByteBuffer.allocate(1024);
		int len;
		try (SeekableByteChannel input = Files.newByteChannel(filePath, StandardOpenOption.READ)) {
			while ((len = input.read(buffer)) > 0) {
				buffer.flip();
				crc.update(buffer.array(), 0, len);
			}
		}
		return crc.getValue();
	}
}
