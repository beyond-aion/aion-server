package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;
import java.util.function.Function;

import com.aionemu.gameserver.model.gameobjects.player.Macros;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Packet with macro list.
 * 
 * @author -Nemesiss-
 */
public class SM_MACRO_LIST extends AionServerPacket {

	public static final int STATIC_BODY_SIZE = 7;
	public static final Function<Macros.Macro, Integer> DYNAMIC_BODY_PART_SIZE_CALCULATOR = (macro) -> 1 + macro.xml().length() * 2 + 2;

	private final int playerObjectId;
	private final List<Macros.Macro> macros;
	private final boolean clearList;
	private final boolean isLast;

	public SM_MACRO_LIST(int playerObjectId, List<Macros.Macro> macros, boolean clearList, boolean isLast) {
		this.playerObjectId = playerObjectId;
		this.macros = macros;
		this.clearList = clearList;
		this.isLast = isLast;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(playerObjectId);
		writeC(clearList ? 1 : 0); // 1 = clears all entries in the macro list before adding the ones sent here
		writeH(isLast ? -macros.size() : macros.size()); // negative marks the last part
		for (Macros.Macro macro : macros) {
			writeC(macro.id());
			writeS(macro.xml());
		}
	}
}
