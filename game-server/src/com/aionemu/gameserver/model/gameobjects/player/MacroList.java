package com.aionemu.gameserver.model.gameobjects.player;

import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Player macrosses collection, contains all player macrosses.
 * <p/>
 * Created on: 13.07.2009 16:28:23
 * 
 * @author Aquanox, nrg
 */
public class MacroList {

	/**
	 * Class logger
	 */
	private static final Logger logger = LoggerFactory.getLogger(MacroList.class);

	/**
	 * Container of macrosses, position to xml.
	 */
	private final Map<Integer, String> macrosses;

	/**
	 * Creates an empty macro list
	 */
	public MacroList() {
		this.macrosses = new HashMap<>(12);
	}

	/**
	 * Create new instance of <tt>MacroList</tt>.
	 * 
	 * @param arg
	 */
	public MacroList(Map<Integer, String> arg) {
		this.macrosses = arg;
	}

	/**
	 * Returns map with all macrosses
	 * 
	 * @return all macrosses
	 */
	public Map<Integer, String> getMacrosses() {
		return Collections.unmodifiableMap(macrosses);
	}

	/**
	 * Add macro to the collection.
	 * 
	 * @param macroPosition
	 *          Macro order.
	 * @param macroXML
	 *          Macro Xml contents.
	 * @return <tt>true</tt> if macro addition was successful, and it can be stored into database. Otherwise <tt>false</tt>.
	 */
	public synchronized boolean addMacro(int macroPosition, String macroXML) {
		if (macrosses.containsKey(macroPosition)) {
			macrosses.remove(macroPosition);
			macrosses.put(macroPosition, macroXML);
			return false;
		}

		macrosses.put(macroPosition, macroXML);
		return true;
	}

	/**
	 * Remove macro from the list.
	 * 
	 * @param macroPosition
	 * @return <tt>true</tt> if macro deletion was successful, and changes can be stored into database. Otherwise <tt>false</tt>.
	 */
	public synchronized boolean removeMacro(int macroPosition) {
		String m = macrosses.remove(macroPosition);
		if (m == null)//
		{
			logger.warn("Trying to remove non existing macro.");
			return false;
		}
		return true;
	}

	/**
	 * Returns count of available macrosses.
	 * 
	 * @return count of available macrosses.
	 */
	public int getSize() {
		return macrosses.size();
	}

	/**
	 * Returns an unmodifiable map of macro id to macro contents. NOTE: Retail sends only 7 macros per packet, that's why we have to split macros
	 */
	public Map<Integer, String> getMarcosPart(boolean secondPart) {
		Map<Integer, String> macrosPart = new LinkedHashMap<>();
		int currentIndex = secondPart ? 7 : 0;
		int endIndex = secondPart ? 12 : 6;

		for (; currentIndex <= endIndex; currentIndex++) {
			macrosPart.put(currentIndex, macrosses.get(currentIndex));
		}
		return Collections.unmodifiableMap(macrosPart);
	}
}
