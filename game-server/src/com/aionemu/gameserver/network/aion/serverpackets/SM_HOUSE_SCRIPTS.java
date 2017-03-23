package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.model.house.PlayerScript;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas
 * @modified Neon
 */
public class SM_HOUSE_SCRIPTS extends AionServerPacket {

	private int address;
	private PlayerScripts scripts;
	Set<Integer> scriptIds;

	/**
	 * This packet updates the script information for a house.<br>
	 * If one or more <tt>scriptIds</tt> are given, only these IDs will be updated, otherwise all existing scripts are updated.
	 * 
	 * @param address
	 * @param scripts
	 * @param scriptIds
	 */
	public SM_HOUSE_SCRIPTS(int address, PlayerScripts scripts, int... scriptIds) {
		this.address = address;
		this.scripts = scripts;
		this.scriptIds = scriptIds.length > 0 ? IntStream.of(scriptIds).boxed().collect(Collectors.toSet()) : scripts.getIds();
	}

	@Override
	protected void writeImpl(AionConnection con) {

		writeD(address); // house address
		writeH(scriptIds.size()); // number of scripts that will be updated
		// write scripts in the order that they were passed (not displayed correctly on login, cuz client pre-sorts by ID -_-)
		for (Integer scriptId : scriptIds) {
			PlayerScript script = scripts.get(scriptId);
			writeC(scriptId); // script ID
			if (script != null && script.getCompressedBytes() != null) {
				byte[] bytes = script.getCompressedBytes();
				writeH(8 + bytes.length); // total following byte size for this script
				writeD(bytes.length); // script size (compressed)
				writeD(script.getUncompressedSize()); // script size (uncompressed)
				writeB(bytes); // script content (compressed)
			} else {
				writeH(0); // if the script with this ID does not exist (removes it from the in-game list)
			}
		}
	}
}
