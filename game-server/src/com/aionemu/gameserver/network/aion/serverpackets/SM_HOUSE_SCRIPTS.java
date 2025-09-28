package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collections;
import java.util.List;
import java.util.function.Function;

import com.aionemu.gameserver.model.house.PlayerScript;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rolandas, Neon, Sykra
 */
public class SM_HOUSE_SCRIPTS extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 6;
	private static final byte[] SCRIPT_PADDING = { -51, -51, -51, -51, -51, -51, -51, -51 }; // values seem to not matter, but length does
	public final static int MAX_COMPRESSED_SCRIPT_SIZE = AionServerPacket.MAX_USABLE_PACKET_BODY_SIZE - STATIC_BODY_SIZE - 11 - SCRIPT_PADDING.length;
	public static final Function<PlayerScript, Integer> DYNAMIC_BODY_PART_SIZE_CALCULATOR = (
		script) -> script.hasData() ? 11 + script.compressedBytes().length + SCRIPT_PADDING.length : 3;

	private final int houseAddress;
	private final List<PlayerScript> scripts;

	public SM_HOUSE_SCRIPTS(int houseAddress, PlayerScript script) {
		this.houseAddress = houseAddress;
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
				writeH(8 + scriptContent.length + SCRIPT_PADDING.length); // total following byte size for this script
				writeD(scriptContent.length + SCRIPT_PADDING.length);
				writeD(script.uncompressedSize());
				writeB(scriptContent);
				writeB(SCRIPT_PADDING);
			} else {
				writeH(0); // removes script from the in-game list
			}
		}
	}
}
