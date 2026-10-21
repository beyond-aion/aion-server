package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.templates.flypath.WindstreamState;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

public class SM_WINDSTREAM extends AionServerPacket {

	private final WindstreamState state;
	private final boolean success;

	public SM_WINDSTREAM(WindstreamState state, boolean success) {
		this.state = state;
		this.success = success;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(state.getId());
		writeC(success ? 1 : 0);
	}
}
