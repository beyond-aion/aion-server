package com.aionemu.gameserver.dataholders.loadingutils;

import java.io.File;
import java.io.Reader;
import java.util.concurrent.Future;

import javax.xml.transform.sax.SAXSource;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.InputSource;

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

	private static final String CACHE_XML_FILE = "./cache/static_data.xml";
	private static final String MAIN_XML_FILE = "./data/static_data/static_data.xml";
	private final static String XML_SCHEMA_FILE = "./data/static_data/static_data.xsd";

	/**
	 * Creates {@link StaticData} object based on xml files, starting from static_data.xml
	 * 
	 * @return StaticData object, containing all game data defined in xml files
	 */
	public static StaticData loadStaticData() {
		log.info("Preparing static data cache file...");
		XmlMerger.MergeResult mergeResult;
		try {
			mergeResult = new XmlMerger(new File(MAIN_XML_FILE), new File(CACHE_XML_FILE)).merge();
		} catch (Throwable e) {
			throw new GameServerError("Error while merging xml files", e);
		}

		try {
			log.info("Processing cache file...");
			// passing the xsd for auto schema validation in JAXBUtil.deserialize slows down the server start, so we validate manually in another thread and don't let the server start on error
			Future<?> validationTask = mergeResult.fileIsModified() ? validateAsync(mergeResult) : null;
			StaticData staticData = JAXBUtil.deserialize(mergeResult.newReader(), StaticData.class);
			staticData.setValidationTask(validationTask);
			return staticData;
		} catch (Throwable e) {
			throw new GameServerError("Error while loading static data", e);
		}
	}

	private static Future<?> validateAsync(XmlMerger.MergeResult mergeResult) {
		return ThreadPoolManager.getInstance().submitLongRunning(() -> {
			log.info("Validating " + mergeResult.getFile() + " in background...");
			try {
				if (!mergeResult.waitUntilFileIsWritten()) {
					throw new RuntimeException();
				}
				long time = System.currentTimeMillis();
				try (Reader reader = mergeResult.newReader()) {
					XmlUtil.getSchema(XML_SCHEMA_FILE).newValidator().validate(new SAXSource(new InputSource(reader)));
				}
				log.info("Validated " + mergeResult.getFile() + " in " + (System.currentTimeMillis() - time) + "ms");
			} catch (Throwable t) {
				mergeResult.getFile().setLastModified(0); // mark file as outdated so validation will run again on next start
				throw new GameServerError("Error validating " + CACHE_XML_FILE, t);
			}
		});
	}
}
