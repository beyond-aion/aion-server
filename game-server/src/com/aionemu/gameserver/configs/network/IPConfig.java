package com.aionemu.gameserver.configs.network;

import java.io.File;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

import com.aionemu.commons.network.IPRange;

/**
 * @author Taran, SoulKeeper Class that is designed to read IPConfig.xml
 */
public class IPConfig {

	/**
	 * Logger
	 */
	private static final Logger log = LoggerFactory.getLogger(IPConfig.class);
	/**
	 * Location of config file
	 */
	private static final String CONFIG_FILE = "./config/network/ipconfig.xml";
	/**
	 * List of all ip ranges
	 */
	private static final List<IPRange> ranges = new ArrayList<IPRange>();
	/**
	 * Default address
	 */
	private static byte[] defaultAddress;

	/**
	 * Method that loads IPConfig
	 */
	public static void load() {
		try {
			SAXParser parser = SAXParserFactory.newInstance().newSAXParser();
			parser.parse(new File(CONFIG_FILE), new DefaultHandler() {

				@Override
				public void startElement(String uri, String localName, String qName, Attributes attributes) throws SAXException {

					if (qName.equals("ipconfig")) {
						try {
							defaultAddress = InetAddress.getByName(attributes.getValue("default")).getAddress();
						} catch (UnknownHostException e) {
							throw new RuntimeException("Failed to resolve DSN for address: " + attributes.getValue("default"), e);
						}
					} else if (qName.equals("iprange")) {
						String min = attributes.getValue("min");
						String max = attributes.getValue("max");
						String address = attributes.getValue("address");
						IPRange ipRange = new IPRange(min, max, address);
						ranges.add(ipRange);
					}
				}
			});
		} catch (Exception e) {
			log.error("Critical error while parsing ipConfig", e);
			throw new Error("Can't load ipConfig", e);
		}
	}

	/**
	 * Returns list of ip ranges
	 * 
	 * @return list of ip ranges
	 */
	public static List<IPRange> getRanges() {
		return ranges;
	}

	/**
	 * Returns default address
	 * 
	 * @return default address
	 */
	public static byte[] getDefaultAddress() {
		return defaultAddress;
	}
}
