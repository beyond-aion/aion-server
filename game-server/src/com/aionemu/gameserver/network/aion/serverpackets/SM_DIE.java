package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author orz
 * @author Sarynth thx Rhys2002 for packets. :)
 */
public class SM_DIE extends AionServerPacket {

	private boolean hasRebirth;
	private boolean hasItem;
	private int remainingKiskTime;
	private int type = 0;
	private boolean invasion;

	public SM_DIE(boolean hasRebirth, boolean hasItem, int remainingKiskTime, int type) {
		this(hasRebirth, hasItem, remainingKiskTime, type, false);
	}

	public SM_DIE(boolean hasRebirth, boolean hasItem, int remainingKiskTime, int type, boolean invasion) {
		this.hasRebirth = hasRebirth;
		this.hasItem = hasItem;
		this.remainingKiskTime = remainingKiskTime;
		this.type = type;
		this.invasion = invasion;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC((hasRebirth ? 1 : 0)); // skillRevive
		writeC((hasItem ? 1 : 0)); // itemRevive
		writeD(remainingKiskTime);
		writeC(type);
		writeC(invasion ? 0x80 : 0x00);
	}

}