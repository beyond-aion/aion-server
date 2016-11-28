package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * Packet with macro list.
 * 
 * @author -Nemesiss-
 */
public class SM_MACRO_LIST extends AionServerPacket {

	private Player player;
	private boolean secondPart;

	/**
	 * Constructs new <tt>SM_MACRO_LIST </tt> packet
	 */
	public SM_MACRO_LIST(Player player, boolean secondPart) {
		this.player = player;
		this.secondPart = secondPart;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(player.getObjectId());// player id

		Map<Integer, String> macrosToSend = player.getMacroList().getMarcosPart(secondPart);
		int size = macrosToSend.size();
		size = -size;

		if (secondPart) {
			writeC(0x00);
		} else {
			writeC(0x01);
		}

		writeH(size);

		if (size != 0) {
			for (Map.Entry<Integer, String> entry : macrosToSend.entrySet()) {
				writeC(entry.getKey());// order
				writeS(entry.getValue());// xml
			}
		}
	}
}
