package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.aionemu.gameserver.model.gameobjects.player.PlayerScripts;
import com.aionemu.gameserver.model.house.PlayerScript;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas, Neon, Sykra
 */
public class SM_HOUSE_SCRIPTS extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 48;
	public static final Function<PlayerScript, Integer> DYNAMIC_BODY_PART_SIZE_CALCULATOR = (
		script) -> script.hasData() ? 88 + script.compressedBytes().length : 24;

	private final int houseAddress;
	private final List<PlayerScript> scripts;

	public SM_HOUSE_SCRIPTS(int houseAddress, PlayerScripts scripts, int scriptId) {
		this.houseAddress = houseAddress;
		PlayerScript script = scripts.get(scriptId);
		this.scripts = script == null ? Collections.emptyList() : Collections.singletonList(script);
	}

	public SM_HOUSE_SCRIPTS(int houseAddress, List<PlayerScript> scripts) {
		this.houseAddress = houseAddress;
		this.scripts = scripts;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(houseAddress);
		writeH(scripts.size());
		for (PlayerScript script : scripts) {
			writeC(script.id());
			if (script.hasData()) {
				byte[] scriptContent = script.compressedBytes();
				writeH(8 + scriptContent.length); // total following byte size for this script
				writeD(scriptContent.length);
				writeD(script.uncompressedSize());
				writeB(scriptContent);
			} else {
				writeH(0); // removes script from the in-game list
			}
		}
	}
}
