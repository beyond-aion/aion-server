package com.aionemu.gameserver.model.gameobjects.player;

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
	/**
	 * The client refuses larger script blocks.
	 */
	public static final int MAX_SCRIPT_BLOCK_SIZE = 3072;
	/**
	 * Two size fields and the client's 8 trailing bytes.
	 */
	private static final int SCRIPT_BLOCK_OVERHEAD = 16;
	private static final int MAX_COMPRESSED_SIZE = MAX_SCRIPT_BLOCK_SIZE - SCRIPT_BLOCK_OVERHEAD;
	/**
	 * The client's compressedSize still includes its 8 trailing bytes.
	 */
	private static final int MAX_CLIENT_CONTENT_SIZE = MAX_SCRIPT_BLOCK_SIZE - 8;
	/**
	 * Above the ~3.1 MB a 3072-byte deflate stream can produce; only rejects impossible or corrupted data.
	 */
	public static final int MAX_UNCOMPRESSED_SCRIPT_SIZE = 4 << 20;
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
		if (compressedXML == null || compressedXML.length == 0) {
			if (uncompressedSize != 0) {
				logInvalid(compressedXML, uncompressedSize);
				return false;
			}
		} else {
			if (compressedXML.length > MAX_CLIENT_CONTENT_SIZE || uncompressedSize <= 0 || uncompressedSize > MAX_UNCOMPRESSED_SCRIPT_SIZE) {
				logInvalid(compressedXML, uncompressedSize);
				return false;
			}
			int streamLength = CompressUtil.zlibStreamLength(compressedXML, uncompressedSize);
			if (streamLength < 0 || streamLength > MAX_COMPRESSED_SIZE) {
				logInvalid(compressedXML, uncompressedSize);
				return false;
			}
			// The client sends back the 8 trailing bytes of the block it received; keeping them would grow the block on every save.
			if (streamLength < compressedXML.length)
				compressedXML = Arrays.copyOf(compressedXML, streamLength);
		}
		if (storeInDb) {
			House house = HousingService.getInstance().findHouseOrStudio(houseObjId);
			if (house.getPersistentState() == PersistentState.NEW)
				house.save(); // new houses must be inserted first, due to foreign key constraints
			HouseScriptsDAO.storeScript(houseObjId, id, compressedXML, uncompressedSize);
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

	private void logInvalid(byte[] compressedXML, int uncompressedSize) {
		log.warn("Invalid housing script data for house {}: {} bytes declared to inflate to {} bytes", houseObjId,
			compressedXML == null ? 0 : compressedXML.length, uncompressedSize);
	}

	public void sendToPlayer(Player player, int houseAddress) {
		if (player == null)
			return;
		SplitList<PlayerScript> scriptSplitList = new DynamicServerPacketBodySplitList<>(Arrays.asList(scripts), false, SM_HOUSE_SCRIPTS.STATIC_BODY_SIZE,
			SM_HOUSE_SCRIPTS.DYNAMIC_BODY_PART_SIZE_CALCULATOR);
		scriptSplitList.forEach(part -> PacketSendUtility.sendPacket(player, new SM_HOUSE_SCRIPTS(houseAddress, part)));
	}

}
