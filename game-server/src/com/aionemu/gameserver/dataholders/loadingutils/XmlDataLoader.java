package com.aionemu.gameserver.dataholders.loadingutils;

import java.io.File;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.xml.JAXBUtil;
import com.aionemu.gameserver.GameServerError;
import com.aionemu.gameserver.dataholders.StaticData;

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

	public static final XmlDataLoader getInstance() {
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
		mergeXmlFiles(cachedXml, cleanMainXml);

		try {
			log.info("Processing cache file...");
			return JAXBUtil.deserialize(cachedXml, StaticData.class, XML_SCHEMA_FILE);
		} catch (Throwable e) {
			throw new GameServerError("Error while loading static data", e);
		}
	}

	/**
	 * Merges xml files(if are newer than cache file) and puts output to cache file.
	 * 
	 * @see XmlMerger
	 * @param cachedXml
	 * @param cleanMainXml
	 * @throws Error
	 *           is thrown if some problem occured.
	 */
	private void mergeXmlFiles(File cachedXml, File cleanMainXml) throws Error {
		XmlMerger merger = new XmlMerger(cleanMainXml, cachedXml);
		try {
			merger.process();
		} catch (Throwable e) {
			throw new GameServerError("Error while merging xml files", e);
		}
	}

	private static class SingletonHolder {

		protected static final XmlDataLoader instance = new XmlDataLoader();
	}
}
