package com.aionemu.gameserver.model.gameobjects.player;

import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.HouseScriptsDAO;
import com.aionemu.gameserver.model.house.PlayerScript;
import com.aionemu.gameserver.utils.xml.CompressUtil;

import javolution.util.FastMap;

/**
 * @author Rolandas
 * @reworked Neon
 */
public class PlayerScripts {

	private static final Logger log = LoggerFactory.getLogger(PlayerScripts.class);
	private static final byte SCRIPT_LIMIT = 8; // max number of active scripts a player can have

	private final int houseObjId;
	private final FastMap<Integer, PlayerScript> scripts;

	public PlayerScripts(int houseId) {
		this.houseObjId = houseId;
		this.scripts = new FastMap<>();
	}

	public boolean set(int id, byte[] compressedXML, int uncompressedSize) {
		return set(id, compressedXML, uncompressedSize, true);
	}

	public boolean set(int id, byte[] compressedXML, int uncompressedSize, boolean doStore) {
		if (count() >= getMaxCount() && !scripts.containsKey(id))
			return false;

		String scriptXML = decompressAndValidate(compressedXML, uncompressedSize);

		if (scriptXML == null)
			return false;

		if (doStore)
			DAOManager.getDAO(HouseScriptsDAO.class).storeScript(houseObjId, id, scriptXML);

		scripts.put(id, new PlayerScript(compressedXML, uncompressedSize));
		return true;
	}

	public boolean remove(int id) {
		return remove(id, true);
	}

	public boolean remove(int id, boolean doStore) {
		if (!scripts.containsKey(id))
			return false;

		if (doStore)
			DAOManager.getDAO(HouseScriptsDAO.class).deleteScript(houseObjId, id);

		scripts.remove(id);
		return true;
	}

	public PlayerScript get(int id) {
		return scripts.get(id);
	}

	public Set<Integer> getIds() {
		return scripts.keySet();
	}

	public int count() {
		return scripts.size();
	}

	public static int getMaxCount() {
		return SCRIPT_LIMIT;
	}

	private String decompressAndValidate(byte[] compressedXML, int uncompressedSize) {
		String scriptXML = "";

		if (compressedXML != null && compressedXML.length > 0) {
			try {
				scriptXML = CompressUtil.decompress(compressedXML);
				byte[] bytes = scriptXML.getBytes("UTF-16LE");
				if (bytes.length != uncompressedSize) {
					log.error("New housing script data had unexpected file size after decompression: Expected " + uncompressedSize + " bytes, got "
						+ bytes.length + " bytes:\n" + scriptXML);
					return null;
				}
			} catch (Exception ex) {
				log.error("New housing script data could not be decompressed");
				return null;
			}
		}

		return scriptXML;
	}
}
