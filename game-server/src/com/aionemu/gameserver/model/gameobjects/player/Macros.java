package com.aionemu.gameserver.model.gameobjects.player;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.aionemu.gameserver.configs.main.CustomConfig;

/**
 * @author Aquanox, nrg
 */
public class Macros {

	private final Map<Integer, Macro> macrosById = new TreeMap<>();

	public synchronized List<Macro> getAll() {
		return new ArrayList<>(macrosById.values());
	}

	/**
	 * @return <tt>true</tt> if given macro ID was not used before.
	 */
	public synchronized boolean add(int macroId, String macroXML) {
		if (!isValidId(macroId))
			throw new IllegalArgumentException("Invalid macro ID: " + macroId);
		return macrosById.put(macroId, new Macro(macroId, macroXML)) == null;
	}

	public static boolean isValidId(int macroId) {
		return macroId >= 1 && macroId <= CustomConfig.MACROS_LIMIT;
	}

	public synchronized boolean remove(int macroId) {
		return macrosById.remove(macroId) != null;
	}

	public record Macro(int id, String xml) {}
}
