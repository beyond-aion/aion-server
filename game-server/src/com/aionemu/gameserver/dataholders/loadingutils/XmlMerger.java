package com.aionemu.gameserver.dataholders.loadingutils;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.CRC32;

import javax.xml.namespace.QName;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.stream.*;
import javax.xml.stream.events.XMLEvent;

import com.fasterxml.aalto.stax.InputFactoryImpl;
import com.fasterxml.aalto.stax.OutputFactoryImpl;
import org.glassfish.jaxb.runtime.v2.util.ByteArrayOutputStreamEx;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.Locator;
import org.xml.sax.SAXException;
import org.xml.sax.SAXParseException;
import org.xml.sax.helpers.DefaultHandler;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.utils.ThreadPoolManager;
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

	private static final Map<Integer, String> COUNTRY_REGION = Map.of(1, "usa", 2, "europe", 4, "japan", 5, "china", 6, "taiwan", 7, "russia");

	private static final byte[] XML_DECLARATION = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>".getBytes(StandardCharsets.UTF_8);

	private final File sourceFile;
	private final File destFile;
	private final File metaDataFile;

	private final XMLInputFactory inputFactory = new InputFactoryImpl();
	private final XMLOutputFactory outputFactory = new OutputFactoryImpl();

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
			return new MergeResult(destFile, null, 0, Split.read(destFile));
		}

		return mergeAndWrite();
	}

	/**
	 * Merges the cache without decoding and re-encoding the imported documents, which is several times faster than the token based merge. If
	 * anything about the source files is unexpected (non UTF-8 files, unusual roots), the token based merge is used instead.
	 */
	private MergeResult mergeAndWrite() throws Exception {
		try {
			return mergeAndWriteBytes();
		} catch (Exception e) {
			log.warn("Byte-level merge failed, falling back to the token based merge", e);
			return mergeAndWriteAsync();
		}
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

		return handler.isModified() || !metadata.unchecked.isEmpty();
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
			writer = outputFactory.createXMLStreamWriter(new BufferedWriter(new OutputStreamWriter(outputStream, StandardCharsets.UTF_8)));
			reader = inputFactory.createXMLStreamReader(fileReader);

			writer.writeStartDocument();

			List<int[]> elementRanges = new ArrayList<>();
			int depth = 0;
			int tailStart = 0;
			while (reader.hasNext()) {
				switch (reader.next()) {
					case XMLEvent.START_DOCUMENT:
					case XMLEvent.END_DOCUMENT:
					case XMLEvent.COMMENT:
					case XMLEvent.SPACE: // ignorable whitespace
						continue;
					case XMLEvent.START_ELEMENT:
						if ("import".equals(reader.getLocalName())) { // import declared file
							writer.flush();
							int start = outputStream.size();
							processImportElement(reader, writer, metadata);
							writer.flush();
							elementRanges.add(new int[] { start, outputStream.size() });
							continue;
						}
						break;
					case XMLEvent.END_ELEMENT: // skip closing import tag, since it's replaced by declared file contents
						if ("import".equals(reader.getLocalName()))
							continue;
						if (depth == 1) { // closing the root element
							writer.flush();
							tailStart = outputStream.size();
						}
						break;
				}

				if (reader.isWhiteSpace())
					continue;

				write(reader, writer);
				if (reader.isStartElement())
					depth++;
				else if (reader.isEndElement())
					depth--;
			}
			writer.writeEndDocument();
			writer.flush();

			// the writer keeps the closing '>' of a start tag pending, so the first capture may point at it instead of at the next element
			byte[] buffer = outputStream.getBuffer();
			List<int[]> fixedRanges = new ArrayList<>(elementRanges.size());
			for (int[] range : elementRanges) {
				int elementStart = range[0];
				while (elementStart < range[1] && buffer[elementStart] != '<')
					elementStart++;
				fixedRanges.add(new int[] { elementStart, range[1] });
			}
			int splitHeaderEnd = fixedRanges.isEmpty() ? 0 : fixedRanges.get(0)[0];

			Split split = new Split(splitHeaderEnd, tailStart, outputStream.size(), fixedRanges);
			MergeResult mergeResult = new MergeResult(destFile, outputStream.getBuffer(), outputStream.size(), split);
			mergeResult.fileWriteTask = ThreadPoolManager.getInstance().submit(() -> {
				destFile.getParentFile().mkdirs();
				try (Reader mergeResultReader = mergeResult.newReader(); Writer fileWriter = Files.newBufferedWriter(destFile.toPath(), StandardCharsets.UTF_8)) {
					mergeResultReader.transferTo(fileWriter);
					metadata.store(metaDataFile);
					split.store(destFile);
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
	 * Copies the imported documents into the cache without decoding them. The documents are UTF-8, so only the xml declaration, the root tags
	 * and the root tags of files merged into a single root have to be handled.
	 */
	private MergeResult mergeAndWriteBytes() throws IOException, XMLStreamException {
		Metadata metadata = new Metadata();
		ByteArrayOutputStreamEx outputStream = new ByteArrayOutputStreamEx(100 * 1024 * 1024);
		byte[] source = Files.readAllBytes(sourceFile.toPath());

		int rootStart = findRootStart(source);
		int rootTagEnd = findTagEnd(source, rootStart);
		String rootName = elementName(source, rootStart);
		outputStream.write(XML_DECLARATION);
		outputStream.write(source, rootStart, rootTagEnd - rootStart + 1);
		int headerEnd = outputStream.size();

		List<int[]> ranges = new ArrayList<>();
		XMLStreamReader reader = inputFactory.createXMLStreamReader(new ByteArrayInputStream(source));
		try {
			while (reader.hasNext()) {
				if (reader.next() != XMLStreamConstants.START_ELEMENT || !"import".equals(reader.getLocalName()))
					continue;
				int start = outputStream.size();
				writeImportBytes(reader, outputStream, metadata);
				ranges.add(new int[] { start, outputStream.size() });
			}
		} finally {
			reader.close();
		}

		int tailStart = outputStream.size();
		outputStream.write(("</" + rootName + ">").getBytes(StandardCharsets.UTF_8));
		int docEnd = outputStream.size();

		Split split = new Split(headerEnd, tailStart, docEnd, ranges);
		MergeResult mergeResult = new MergeResult(destFile, outputStream.getBuffer(), docEnd, split);
		mergeResult.fileWriteTask = ThreadPoolManager.getInstance().submit(() -> {
			try {
				destFile.getParentFile().mkdirs();
				try (OutputStream fileOut = Files.newOutputStream(destFile.toPath())) {
					fileOut.write(mergeResult.getBuffer(), 0, docEnd);
				}
				metadata.store(metaDataFile);
				split.store(destFile);
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
		});
		return mergeResult;
	}

	private void writeImportBytes(XMLStreamReader reader, OutputStream out, Metadata metadata) throws IOException, XMLStreamException {
		File file = new File(sourceFile.getParent(), getAttributeValue(reader, qNameFile, null));
		file = applyCountryOverride(file);
		if (!file.exists())
			throw new FileNotFoundException("Missing file to import:" + file.getPath());

		if (file.isFile()) {
			writeElementBytes(file, true, true, out, metadata);
			return;
		}

		boolean singleRootTag = Boolean.parseBoolean(getAttributeValue(reader, qNameSingleRootTag, "false"));
		boolean recursive = Boolean.parseBoolean(getAttributeValue(reader, qNameRecursiveImport, "true"));
		String rootName = null;
		for (File childFile : XmlUtil.listFiles(file, recursive)) {
			if (rootName == null)
				rootName = writeElementBytes(childFile, true, !singleRootTag, out, metadata);
			else
				writeElementBytes(childFile, !singleRootTag, !singleRootTag, out, metadata);
		}
		if (singleRootTag) {
			if (rootName == null)
				throw new IOException("No files to import in " + file);
			out.write(("</" + rootName + ">").getBytes(StandardCharsets.UTF_8));
		}
	}

	private String writeElementBytes(File file, boolean writeStartTag, boolean writeEndTag, OutputStream out, Metadata metadata) throws IOException {
		metadata.add(file);
		byte[] bytes = Files.readAllBytes(file.toPath());
		if (!isUtf8(bytes))
			throw new UnsupportedEncodingException(file + " is not UTF-8");

		int rootStart = findRootStart(bytes);
		int rootTagEnd = findTagEnd(bytes, rootStart);
		String name = elementName(bytes, rootStart);
		if (isSelfClosing(bytes, rootStart, rootTagEnd)) {
			if (writeStartTag)
				out.write(bytes, rootStart, rootTagEnd - rootStart + 1);
			return name;
		}

		int rootEndStart = findRootEnd(bytes, name);
		if (writeStartTag)
			out.write(bytes, rootStart, rootTagEnd - rootStart + 1);
		out.write(bytes, rootTagEnd + 1, rootEndStart - rootTagEnd - 1);
		if (writeEndTag) {
			int rootEndTagEnd = findTagEnd(bytes, rootEndStart);
			out.write(bytes, rootEndStart, rootEndTagEnd - rootEndStart + 1);
		}
		return name;
	}

	private static int findRootStart(byte[] bytes) {
		int i = 0;
		if (bytes.length >= 3 && (bytes[0] & 0xFF) == 0xEF && (bytes[1] & 0xFF) == 0xBB && (bytes[2] & 0xFF) == 0xBF)
			i = 3;
		while (i < bytes.length) {
			byte b = bytes[i];
			if (b == '<') {
				if (i + 1 < bytes.length && (bytes[i + 1] == '?' || bytes[i + 1] == '!')) {
					if (i + 3 < bytes.length && bytes[i + 1] == '!' && bytes[i + 2] == '-' && bytes[i + 3] == '-')
						i = skipUntil(bytes, i + 4, "-->");
					else
						i = skipUntil(bytes, i + 2, ">");
					continue;
				}
				if (i + 1 < bytes.length && bytes[i + 1] == '/')
					throw new IllegalArgumentException("Unexpected closing tag before the root element");
				return i;
			}
			if (!isWhitespace(b))
				throw new IllegalArgumentException("Unexpected content before the root element");
			i++;
		}
		throw new IllegalArgumentException("No root element found");
	}

	private static int findTagEnd(byte[] bytes, int tagStart) {
		byte quote = 0;
		for (int i = tagStart + 1; i < bytes.length; i++) {
			byte b = bytes[i];
			if (quote != 0) {
				if (b == quote)
					quote = 0;
			} else if (b == '"' || b == '\'') {
				quote = b;
			} else if (b == '>') {
				return i;
			}
		}
		throw new IllegalArgumentException("Unterminated tag at offset " + tagStart);
	}

	private static String elementName(byte[] bytes, int tagStart) {
		int i = tagStart + 1;
		while (i < bytes.length && !isWhitespace(bytes[i]) && bytes[i] != '>' && bytes[i] != '/')
			i++;
		return new String(bytes, tagStart + 1, i - tagStart - 1, StandardCharsets.UTF_8);
	}

	private static boolean isSelfClosing(byte[] bytes, int tagStart, int tagEnd) {
		for (int i = tagEnd - 1; i > tagStart; i--) {
			if (!isWhitespace(bytes[i]))
				return bytes[i] == '/';
		}
		return false;
	}

	private static int findRootEnd(byte[] bytes, String name) {
		byte[] closing = ("</" + name).getBytes(StandardCharsets.UTF_8);
		for (int i = bytes.length - closing.length - 1; i > 0; i--) {
			if (matches(bytes, i, closing)) {
				int next = i + closing.length;
				if (next >= bytes.length || isWhitespace(bytes[next]) || bytes[next] == '>')
					return i;
			}
		}
		throw new IllegalArgumentException("No closing tag for root element " + name);
	}

	private static int skipUntil(byte[] bytes, int from, String token) {
		byte[] bytesToken = token.getBytes(StandardCharsets.US_ASCII);
		for (int i = from; i <= bytes.length - bytesToken.length; i++) {
			if (matches(bytes, i, bytesToken))
				return i + bytesToken.length;
		}
		throw new IllegalArgumentException("Unterminated " + token);
	}

	private static boolean matches(byte[] bytes, int offset, byte[] pattern) {
		if (offset + pattern.length > bytes.length)
			return false;
		for (int i = 0; i < pattern.length; i++) {
			if (bytes[offset + i] != pattern[i])
				return false;
		}
		return true;
	}

	private static boolean isWhitespace(byte b) {
		return b == ' ' || b == '\t' || b == '\r' || b == '\n';
	}

	private static boolean isUtf8(byte[] bytes) {
		int limit = Math.min(bytes.length, 256);
		String head = new String(bytes, 0, limit, StandardCharsets.ISO_8859_1); // the declaration is ASCII, so bytes map to chars one by one
		int encoding = head.indexOf("encoding");
		if (encoding < 0)
			return true;
		int equals = head.indexOf('=', encoding);
		if (equals < 0)
			return true;
		int i = equals + 1;
		while (i < head.length() && Character.isWhitespace(head.charAt(i)))
			i++;
		if (i >= head.length() || (head.charAt(i) != '"' && head.charAt(i) != '\''))
			return true;
		char quote = head.charAt(i++);
		int end = head.indexOf(quote, i);
		if (end < 0)
			return true;
		String value = head.substring(i, end).trim();
		return value.isEmpty() || value.equalsIgnoreCase("UTF-8") || value.equalsIgnoreCase("UTF8");
	}

	/**
	 * This method processes the 'import' element, replacing it by the data from the relevant files.
	 */
	private void processImportElement(XMLStreamReader reader, XMLStreamWriter writer, Metadata metadata) throws XMLStreamException, IOException {
		File file = new File(sourceFile.getParent(), getAttributeValue(reader, qNameFile, null));
		file = applyCountryOverride(file);

		if (!file.exists())
			throw new FileNotFoundException("Missing file to import:" + file.getPath());

		QName startElement = null;
		if (file.isFile()) {
			importFile(file, false, false, writer, metadata);
		} else {
			boolean singleRootTag = Boolean.parseBoolean(getAttributeValue(reader, qNameSingleRootTag, "false"));
			boolean recImport = Boolean.parseBoolean(getAttributeValue(reader, qNameRecursiveImport, "true"));
			log.debug("Processing dir {}", file);
			for (File file2 : XmlUtil.listFiles(file, recImport)) {
				boolean skipRootStartElement = singleRootTag && startElement != null;
				startElement = importFile(file2, skipRootStartElement, singleRootTag, writer, metadata);
			}
			writer.writeEndElement();
		}
	}

	/**
	 * Returns the region specific variant of the imported file if it exists and matches the configured country code, otherwise the file itself.
	 */
	private static File applyCountryOverride(File file) {
		String region = COUNTRY_REGION.get(GSConfig.SERVER_COUNTRY_CODE);
		if (region == null)
			return file;
		String name = file.getName();
		int dot = name.lastIndexOf('.');
		String base = dot < 0 ? name : name.substring(0, dot);
		String ext = dot < 0 ? "" : name.substring(dot);
		File override = new File(file.getParentFile(), base + '_' + region + ext);
		return override.isFile() ? override : file;
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
		log.debug("Appending file {}", file);
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
			file = applyCountryOverride(file);

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
				log.warn("File verification error. File: {}, location={}:{}", file.getPath(), locator.getLineNumber(), locator.getColumnNumber(), e);
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
		private final Set<String> unchecked = new HashSet<>();

		void add(File file) throws IOException {
			properties.setProperty(file.getPath(), file.length() + ":" + file.lastModified() + ":" + crc(file));
		}

		long crc(File file) throws IOException {
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
			String key = file.getPath();
			String data = properties.getProperty(key);
			unchecked.remove(key);
			if (data == null)
				return true;
			// fast path: the file is untouched, so there is no need to read and hash all imports on every start
			if (data.startsWith(file.length() + ":" + file.lastModified() + ":"))
				return false;
			// metadata written by an older version, or only the modification time changed: compare the contents
			String crcValue = String.valueOf(crc(file));
			return !data.equals(crcValue) && !data.endsWith(":" + crcValue);
		}

		void load(File file) throws IOException {
			try (BufferedReader reader = Files.newBufferedReader(file.toPath())) {
				properties.load(reader);
				unchecked.clear();
				unchecked.addAll(properties.stringPropertyNames());
			}
		}

		void store(File file) throws IOException {
			try (BufferedWriter writer = Files.newBufferedWriter(file.toPath())) {
				properties.store(writer, " This file is machine-generated. DO NOT EDIT!");
			}
		}
	}

	/**
	 * Byte ranges of the top-level elements inside the merged cache file, used to unmarshal them in parallel. The sidecar file is written
	 * together with the cache and is only trusted when its size matches the cache file.
	 */
	public static class Split {

		private static final String FILE_SUFFIX = ".ranges";

		private final int headerEnd;
		private final int tailStart;
		private final int docEnd;
		private final int[][] ranges;

		public Split(int headerEnd, int tailStart, int docEnd, List<int[]> ranges) {
			this.headerEnd = headerEnd;
			this.tailStart = tailStart;
			this.docEnd = docEnd;
			this.ranges = ranges.toArray(new int[ranges.size()][]);
		}

		public int[][] getRanges() {
			return ranges;
		}

		private void store(File destFile) throws IOException {
			try (Writer writer = Files.newBufferedWriter(new File(destFile.getPath() + FILE_SUFFIX).toPath())) {
				writer.write(headerEnd + " " + tailStart + " " + docEnd + "\n");
				for (int[] range : ranges)
					writer.write(range[0] + " " + range[1] + "\n");
			}
		}

		static Split read(File destFile) {
			File rangesFile = new File(destFile.getPath() + FILE_SUFFIX);
			if (!rangesFile.isFile())
				return null;
			try {
				List<String> lines = Files.readAllLines(rangesFile.toPath());
				if (lines.isEmpty())
					return null;
				String[] header = lines.get(0).trim().split(" ");
				if (header.length != 3)
					return null;
				int headerEnd = Integer.parseInt(header[0]);
				int tailStart = Integer.parseInt(header[1]);
				int docEnd = Integer.parseInt(header[2]);
				if (Files.size(destFile.toPath()) != docEnd || headerEnd < 0 || headerEnd > tailStart || tailStart > docEnd)
					return null;
				List<int[]> ranges = new ArrayList<>(lines.size() - 1);
				for (int i = 1; i < lines.size(); i++) {
					String[] range = lines.get(i).trim().split(" ");
					if (range.length != 2)
						return null;
					int start = Integer.parseInt(range[0]);
					int end = Integer.parseInt(range[1]);
					if (start < headerEnd || end > tailStart || start >= end)
						return null;
					ranges.add(new int[] { start, end });
				}
				return ranges.isEmpty() ? null : new Split(headerEnd, tailStart, docEnd, ranges);
			} catch (IOException | NumberFormatException e) {
				log.warn("Could not read {}: {}", rangesFile, e.toString());
				return null;
			}
		}
	}

	public static class MergeResult {

		private final File file;
		private final byte[] buffer;
		private final int size;
		private final Split split;
		private Future<?> fileWriteTask;

		public MergeResult(File file, byte[] buffer, int size, Split split) {
			this.file = file;
			this.buffer = buffer;
			this.size = size;
			this.split = split;
		}

		public File getFile() {
			return file;
		}

		public boolean fileIsModified() {
			return buffer != null;
		}

		public Split getSplit() {
			return split;
		}

		/**
		 * @return the merged document held in memory after a merge, or null if the cache file was already up to date
		 */
		public byte[] getBuffer() {
			return buffer;
		}

		public Reader newReader() throws IOException {
			if (buffer == null) {
				return Files.newBufferedReader(file.toPath(), StandardCharsets.UTF_8);
			} else {
				return new InputStreamReader(new ByteArrayInputStream(buffer, 0, size), StandardCharsets.UTF_8);
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
