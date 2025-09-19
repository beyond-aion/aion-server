package com.aionemu.gameserver.model.gameobjects.player;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.HouseScriptsDAO;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.PlayerScript;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_SCRIPTS;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.collections.DynamicServerPacketBodySplitList;
import com.aionemu.gameserver.utils.collections.SplitList;
import com.aionemu.gameserver.utils.xml.CompressUtil;

/**
 * @author Rolandas, Neon, Sykra
 */
public class PlayerScripts {

	public static final byte SCRIPT_LIMIT = 8; // max number of scripts a player can have
	private static final Logger log = LoggerFactory.getLogger(PlayerScripts.class);

	private final int houseObjId;
	private final PlayerScript[] scripts = new PlayerScript[SCRIPT_LIMIT];

	public PlayerScripts(int houseId) {
		this.houseObjId = houseId;
		fillEmptyScriptsArray();
	}

	private void fillEmptyScriptsArray() {
		for (int i = 0; i < SCRIPT_LIMIT; i++)
			scripts[i] = new PlayerScript(i, null, 0);
	}

	public boolean set(int id, byte[] compressedXML, int uncompressedSize) {
		return set(id, compressedXML, uncompressedSize, true);
	}

	public boolean set(int id, byte[] compressedXML, int uncompressedSize, boolean storeInDb) {
		if (isInvalidScriptId(id))
			return false;
		String scriptXML = decompressAndValidate(compressedXML, uncompressedSize);
		if (scriptXML == null)
			return false;
		if (storeInDb) {
			House house = HousingService.getInstance().findHouseOrStudio(houseObjId);
			if (house.getPersistentState() == PersistentState.NEW)
				house.save(); // new houses must be inserted first, due to foreign key constraints
			HouseScriptsDAO.storeScript(houseObjId, id, scriptXML);
		}
		scripts[id] = new PlayerScript(id, compressedXML, uncompressedSize);
		return true;
	}

	public void remove(int scriptId) {
		if (isInvalidScriptId(scriptId))
			return;
		HouseScriptsDAO.deleteScript(houseObjId, scriptId);
		scripts[scriptId] = new PlayerScript(scriptId, null, 0);
	}

	public void removeAll() {
		HouseScriptsDAO.deleteScriptsForHouse(houseObjId);
		fillEmptyScriptsArray();
	}

	public PlayerScript get(int scriptId) {
		if (isInvalidScriptId(scriptId))
			return null;
		return scripts[scriptId];
	}

	private boolean isInvalidScriptId(int scriptId) {
		return scriptId < 0 || scriptId >= scripts.length;
	}

	private String decompressAndValidate(byte[] compressedXML, int uncompressedSize) {
		if (compressedXML == null || compressedXML.length == 0)
			return "";
		byte[] bytes;
		try {
			bytes = CompressUtil.decompress(compressedXML);
		} catch (Exception ex) {
			log.error("Housing script data for house {} could not be decompressed", houseObjId, ex);
			return null;
		}
		String scriptXML = new String(bytes, StandardCharsets.UTF_16LE);
		if (bytes.length != uncompressedSize) {
			log.warn("Unexpected housing script size after decompression for house {}: Expected {} bytes, got {} bytes:\n{}", houseObjId, uncompressedSize,
				bytes.length, scriptXML);
			return null;
		}
		return scriptXML;
	}

	public void sendToPlayer(Player player, int houseAddress) {
		if (player == null)
			return;
		SplitList<PlayerScript> scriptSplitList = new DynamicServerPacketBodySplitList<>(Arrays.asList(scripts), false, SM_HOUSE_SCRIPTS.STATIC_BODY_SIZE,
			SM_HOUSE_SCRIPTS.DYNAMIC_BODY_PART_SIZE_CALCULATOR);
		scriptSplitList.forEach(part -> PacketSendUtility.sendPacket(player, new SM_HOUSE_SCRIPTS(houseAddress, part)));
	}

}
