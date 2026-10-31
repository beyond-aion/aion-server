package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.DuelResult;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * S_DUEL. Type 0 names the opponent and starts the client side countdown, type 1 reports how the duel ended.
 *
 * @author xavier
 */
public class SM_DUEL extends AionServerPacket {

	private final Type type;
	private final int opponentObjId;
	private final DuelResult duelResult;
	private final String playerName;

	private SM_DUEL(Type type, int opponentObjId, DuelResult duelResult, String playerName) {
		this.type = type;
		this.opponentObjId = opponentObjId;
		this.duelResult = duelResult;
		this.playerName = playerName;
	}

	public static SM_DUEL duelStarted(int opponentObjId) {
		return new SM_DUEL(Type.STARTED, opponentObjId, null, null);
	}

	public static SM_DUEL duelResult(DuelResult result, String playerName) {
		return new SM_DUEL(Type.RESULT, 0, result, playerName);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(type.getId());

		switch (type) {
			case STARTED -> writeD(opponentObjId);
			case RESULT -> {
				writeC(duelResult.getResultCode());
				writeD(duelResult.getMsgId());
				writeS(playerName);
			}
		}
	}

	private enum Type {
		STARTED(0),
		RESULT(1);

		private final int id;

		Type(int id) {
			this.id = id;
		}

		private int getId() {
			return id;
		}
	}
}
