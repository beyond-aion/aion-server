package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author -Avol-
 */
public class SM_EXCHANGE_CONFIRMATION extends AionServerPacket {

	private final Action action;

	public SM_EXCHANGE_CONFIRMATION(Action action) {
		this.action = action;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(action.id);
	}

	public enum Action {
		COMPLETED(0),
		CANCELED(1),
		PARTNER_CONFIRMED(2),
		PARTNER_LOCKED(3);

		private final int id;

		Action(int id) {
			this.id = id;
		}
	}
}
