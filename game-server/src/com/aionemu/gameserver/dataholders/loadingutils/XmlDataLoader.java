package com.aionemu.gameserver.dataholders.loadingutils;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.concurrent.Future;

import javax.xml.stream.XMLInputFactory;
import javax.xml.stream.XMLStreamReader;
import javax.xml.transform.stax.StAXSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dataholders.StaticData;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.xml.JAXBUtil;
import com.aionemu.gameserver.utils.xml.XmlUtil;

/**
 * This class is responsible for loading xml files. It uses JAXB to do the job.<br>
 * In addition, it uses @{link {@link XmlMerger} to create input file from all xml files.
 * 
 * @author Luno
 */
public class XmlDataLoader {

	private static final Logger log = LoggerFactory.getLogger(XmlDataLoader.class);

	/** File containing xml schema declaration */
	private final static String XML_SCHEMA_FILE = "./data/static_data/static_data.xsd";

	private static final String CACHE_DIRECTORY = "./cache";
	private static final String CACHE_XML_FILE = CACHE_DIRECTORY + "/static_data.xml";
	private static final String MAIN_XML_FILE = "./data/static_data/static_data.xml";

	public static XmlDataLoader getInstance() {
		return SingletonHolder.instance;
	}

	private XmlDataLoader() {
	}

	/**
	 * Creates {@link StaticData} object based on xml files, starting from static_data.xml
	 * 
	 * @return StaticData object, containing all game data defined in xml files
	 */
	public StaticData loadStaticData() {
		new File(CACHE_DIRECTORY).mkdirs();
		File cachedXml = new File(CACHE_XML_FILE);
		File cleanMainXml = new File(MAIN_XML_FILE);

		log.info("Merging static data into cache file...");
		boolean updated = mergeXmlFiles(cachedXml, cleanMainXml);

		try {
			log.info("Processing cache file...");
			// passing the xsd for auto schema validation in JAXBUtil.deserialize slows down the server start, so we validate manually in another thread and don't let the server start on error
			Future<?> validationTask = updated ? validateAsync(cachedXml) : null;
			StaticData staticData = JAXBUtil.deserialize(cachedXml, StaticData.class);
			staticData.setValidationTask(validationTask);
			return staticData;
		} catch (Throwable e) {
			throw new GameServerError("Error while loading static data", e);
		}
	}

	private Future<?> validateAsync(File cachedXml) {
		return ThreadPoolManager.getInstance().submit(() -> {
			log.info("Validating " + cachedXml + " in background...");
			try (InputStreamReader isr = new InputStreamReader(new FileInputStream(cachedXml), StandardCharsets.UTF_8)) {
				long time = System.currentTimeMillis();
				XMLStreamReader xmlStreamReader = XMLInputFactory.newInstance().createXMLStreamReader(isr);
				XmlUtil.getSchema(XML_SCHEMA_FILE).newValidator().validate(new StAXSource(xmlStreamReader));
				log.info("Validated " + cachedXml + " in " + (System.currentTimeMillis() - time) + "ms");
			} catch (Throwable t) {
				cachedXml.setLastModified(0); // mark file as outdated so validation will run again on next start
				throw new GameServerError("Error validating " + cachedXml, t);
			}
		});
	}

	/**
	 * Merges xml files(if are newer than cache file) and puts output to cache file.
	 *
	 * @return True if the file was created/updated, false if cachedXml is already up to date.
	 */
	private boolean mergeXmlFiles(File cachedXml, File cleanMainXml) throws Error {
		XmlMerger merger = new XmlMerger(cleanMainXml, cachedXml);
		try {
			return merger.process();
		} catch (Throwable e) {
			throw new GameServerError("Error while merging xml files", e);
		}
	}

	private static class SingletonHolder {

		protected static final XmlDataLoader instance = new XmlDataLoader();
	}
}
