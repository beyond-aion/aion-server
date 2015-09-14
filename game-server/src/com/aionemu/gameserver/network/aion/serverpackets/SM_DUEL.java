package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.DuelResult;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author xavier
 */
public class SM_DUEL extends AionServerPacket {

	private String playerName;
	private DuelResult result;
	private int requesterObjId;
	private int type;

	private SM_DUEL(int type) {
		this.type = type;
	}

	public static SM_DUEL SM_DUEL_STARTED(int requesterObjId) {
		SM_DUEL packet = new SM_DUEL(0x00);
		packet.setRequesterObjId(requesterObjId);
		return packet;
	}

	private void setRequesterObjId(int requesterObjId) {
		this.requesterObjId = requesterObjId;
	}

	public static SM_DUEL SM_DUEL_RESULT(DuelResult result, String playerName) {
		SM_DUEL packet = new SM_DUEL(0x01);
		packet.setPlayerName(playerName);
		packet.setResult(result);
		return packet;
	}

	private void setPlayerName(String playerName) {
		this.playerName = playerName;
	}

	private void setResult(DuelResult result) {
		this.result = result;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(type);

		switch (type) {
			case 0x00:
				writeD(requesterObjId);
				break;
			case 0x01:
				writeC(result.getResultId()); // unknown
				writeD(result.getMsgId());
				writeS(playerName);
				break;
			case 0xE0:
				break;
			default:
				throw new IllegalArgumentException("invalid SM_DUEL packet type " + type);
		}
	}
}
