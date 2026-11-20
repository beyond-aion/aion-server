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
	private final boolean fullList;

	private SM_HOUSE_SCRIPTS(int houseAddress, List<PlayerScript> scripts, boolean fullList) {
		this.houseAddress = houseAddress;
		this.scripts = scripts;
		this.fullList = fullList;
	}

	/**
	 * A complete script list. Empty slots are written as {@code 0xFFFF}, which the client treats as "no script".
	 */
	public static SM_HOUSE_SCRIPTS fullList(int houseAddress, List<PlayerScript> scripts) {
		return new SM_HOUSE_SCRIPTS(houseAddress, scripts, true);
	}

	/**
	 * A single script echo for a save or delete. An empty script is written as {@code 0}, which makes the client delete that slot.
	 */
	public static SM_HOUSE_SCRIPTS single(int houseAddress, PlayerScript script) {
		return new SM_HOUSE_SCRIPTS(houseAddress, script == null ? Collections.emptyList() : Collections.singletonList(script), false);
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
				writeH(fullList ? 0xFFFF : 0);
			}
		}
	}
}
