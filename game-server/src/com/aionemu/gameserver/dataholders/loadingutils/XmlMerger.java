package com.aionemu.gameserver.dataholders.loadingutils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.file.Files;
import java.util.Properties;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.CRC32;

import javax.xml.namespace.QName;
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

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.xml.XmlUtil;
import com.sun.xml.bind.v2.util.ByteArrayOutputStreamEx;

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
 *          'recursiveImport':
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

	private final File sourceFile;
	private final File destFile;
	private final File metaDataFile;

	private final XMLInputFactory inputFactory = XMLInputFactory.newInstance();
	private final XMLOutputFactory outputFactory = XMLOutputFactory.newInstance();

	public XmlMerger(File source, File target) {
		this.sourceFile = source;
		this.destFile = target;
		this.metaDataFile = new File(target.getParent(), target.getName() + ".properties");
		inputFactory.setProperty("http://java.sun.com/xml/stream/properties/report-cdata-event", Boolean.TRUE);
	}

	/**
	 * Writes a merged xml if it is missing or updates existing one if the source file or its imports have been modified.
	 */
	public MergeResult merge() throws Exception {
		if (!sourceFile.isFile())
			throw new FileNotFoundException("Source file " + sourceFile.getPath() + " not found.");

		if (!destFile.isFile()) {
			log.info("Cache file not found. Updating...");
		} else if (!metaDataFile.isFile()) {
			log.info("Metadata file not found. Updating...");
		} else if (checkFileModifications()) {
			log.info("Modifications found. Updating...");
		} else {
			log.info("Cache file is up to date");
			return new MergeResult(destFile, null, 0);
		}

		return mergeAndWriteAsync();
	}

	/**
	 * Check for modifications of included files.
	 * 
	 * @return <code>true</code> if at least one of included files has modifications.
	 */
	private boolean checkFileModifications() throws Exception {
		if (sourceFile.lastModified() > destFile.lastModified()) {
			log.debug("Source file was modified ");
			return true;
		}

		Metadata metadata = new Metadata();
		metadata.load(metaDataFile);
		ImportFileHashChecker handler = new ImportFileHashChecker(sourceFile.getParentFile(), metadata);
		SAXParserFactory.newInstance().newSAXParser().parse(sourceFile, handler);

		return handler.isModified();
	}

	/**
	 * This method processes the source file, replacing all of the 'import' tags by the data from the relevant files.
	 */
	private MergeResult mergeAndWriteAsync() throws XMLStreamException, IOException {
		XMLStreamReader reader = null;
		XMLStreamWriter writer = null;

		Metadata metadata = new Metadata();

		try (FileReader fileReader = new FileReader(sourceFile)) {
			ByteArrayOutputStreamEx outputStream = new ByteArrayOutputStreamEx(100 * 1024 * 1024);
			writer = outputFactory.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(outputStream)));
			reader = inputFactory.createXMLStreamReader(fileReader);

			writer.writeStartDocument();
			while (reader.hasNext()) {
				switch (reader.next()) {
					case XMLEvent.START_DOCUMENT:
					case XMLEvent.END_DOCUMENT:
					case XMLEvent.COMMENT:
					case XMLEvent.SPACE: // ignorable whitespace
						continue;
					case XMLEvent.START_ELEMENT:
						if ("import".equals(reader.getLocalName())) { // import declared file
							processImportElement(reader, writer, metadata);
							continue;
						}
						break;
					case XMLEvent.END_ELEMENT: // skip closing import tag, since it's replaced by declared file contents
						if ("import".equals(reader.getLocalName()))
							continue;
				}

				if (reader.isWhiteSpace())
					continue;

				write(reader, writer);
			}
			writer.writeEndDocument();
			writer.flush();

			MergeResult mergeResult = new MergeResult(destFile, outputStream.getBuffer(), outputStream.size());
			mergeResult.fileWriteTask = ThreadPoolManager.getInstance().submit(() -> {
				destFile.getParentFile().mkdirs();
				try (Reader mergeResultReader = mergeResult.newReader(); FileWriter fileWriter = new FileWriter(destFile)) {
					mergeResultReader.transferTo(fileWriter);
					metadata.store(metaDataFile);
				} catch (IOException e) {
					throw new RuntimeException(e);
				}
			});
			return mergeResult;
		} finally {
			if (writer != null)
				writer.close();
			if (reader != null)
				reader.close();
		}
	}

	/**
	 * This method processes the 'import' element, replacing it by the data from the relevant files.
	 */
	private void processImportElement(XMLStreamReader reader, XMLStreamWriter writer, Metadata metadata) throws XMLStreamException, IOException {
		File file = new File(sourceFile.getParent(), getAttributeValue(reader, qNameFile, null));

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
	 * Extract an attribute value from a <code>StartElement</code> event.
	 */
	private String getAttributeValue(XMLStreamReader reader, QName name, String defaultValue) throws XMLStreamException {
		String attribute = reader.getAttributeValue(null, name.getLocalPart());

		if (attribute == null) {
			if (defaultValue == null)
				throw new XMLStreamException("Attribute '" + name.getLocalPart() + "' is missing or empty.", reader.getLocation());

			return defaultValue;
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
	 */
	private QName importFile(File file, boolean skipStartElement, boolean skipEndElement, XMLStreamWriter writer, Metadata metadata)
		throws XMLStreamException, IOException {
		log.debug("Appending file " + file);
		metadata.add(file);

		XMLStreamReader reader = null;

		try (FileReader fileReader = new FileReader(file)) {
			reader = inputFactory.createXMLStreamReader(fileReader);

			QName startElement = null;

			while (reader.hasNext()) {
				switch (reader.next()) {
					case XMLEvent.START_DOCUMENT:
					case XMLEvent.END_DOCUMENT:
					case XMLEvent.COMMENT:
					case XMLEvent.SPACE: // ignorable whitespace
						continue;
				}

				if (reader.isWhiteSpace())
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

			for (int i = 0, len = reader.getAttributeCount(); i < len; i++) {
				String attUri = reader.getAttributeNamespace(i);
				if (attUri == null)
					writer.writeAttribute(reader.getAttributeLocalName(i), reader.getAttributeValue(i));
				else
					writer.writeAttribute(reader.getAttributePrefix(i), attUri, reader.getAttributeLocalName(i), reader.getAttributeValue(i));
			}
		} else if (reader.isEndElement()) {
			writer.writeEndElement();
		} else if (reader.isCharacters()) {
			writer.writeCharacters(reader.getTextCharacters(), reader.getTextStart(), reader.getTextLength());
		} else if (reader.getEventType() == XMLEvent.CDATA) {
			writer.writeCData(reader.getText());
		}
	}

	private static class ImportFileHashChecker extends DefaultHandler {

		private final File basedir;
		private final Metadata metadata;
		private boolean isModified = false;
		private Locator locator;

		private ImportFileHashChecker(File basedir, Metadata metadata) {
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

			try {
				if (file.isFile()) {
					isModified |= metadata.checkIsNewOrModified(file);
				} else if (file.isDirectory()) {
					String rec = attributes.getValue(qNameRecursiveImport.getLocalPart());
					for (File childFile : XmlUtil.listFiles(file, rec == null || Boolean.parseBoolean(rec))) {
						if (metadata.checkIsNewOrModified(childFile)) {
							isModified = true;
							return;
						}
					}
				}
			} catch (IOException e) {
				log.warn("File verification error. File: " + file.getPath() + ", location=" + locator.getLineNumber() + ":" + locator.getColumnNumber(), e);
				isModified = true;
			}
		}

		public boolean isModified() {
			return isModified;
		}
	}

	private class Metadata {

		private final Properties properties = new Properties();
		private final CRC32 crc = new CRC32();
		private final ByteBuffer buffer = ByteBuffer.allocate(64 * 1024);

		void add(File file) throws IOException {
			properties.setProperty(file.getPath(), String.valueOf(hash(file)));
		}

		long hash(File file) throws IOException {
			crc.reset();
			buffer.clear();
			try (FileChannel input = FileChannel.open(file.toPath())) {
				while (input.read(buffer.position(0)) > 0) {
					buffer.flip();
					crc.update(buffer);
				}
			}
			return crc.getValue();
		}

		boolean checkIsNewOrModified(File file) throws IOException {
			String data = properties.getProperty(file.getPath());
			return data == null || !data.equals(String.valueOf(hash(file)));
		}

		void load(File file) throws IOException {
			try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
				properties.load(reader);
			}
		}

		void store(File file) throws IOException {
			try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
				properties.store(writer, " This file is machine-generated. DO NOT EDIT!");
			}
		}
	}

	public static class MergeResult {

		private final File file;
		private final byte[] buffer;
		private final int size;
		private Future<?> fileWriteTask;

		public MergeResult(File file, byte[] buffer, int size) {
			this.file = file;
			this.buffer = buffer;
			this.size = size;
		}

		public File getFile() {
			return file;
		}

		public boolean fileIsModified() {
			return buffer != null;
		}

		public Reader newReader() throws IOException {
			if (buffer == null) {
				return Files.newBufferedReader(file.toPath());
			} else {
				return new InputStreamReader(new ByteArrayInputStream(buffer, 0, size));
			}
		}

		public boolean waitUntilFileIsWritten() {
			try {
				return fileWriteTask == null || fileWriteTask.get() == null && !fileWriteTask.isCancelled();
			} catch (InterruptedException | ExecutionException e) {
				return false;
			}
		}
	}
}
