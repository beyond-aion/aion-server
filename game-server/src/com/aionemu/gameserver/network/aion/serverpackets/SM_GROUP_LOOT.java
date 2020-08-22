package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rhys2002, Sykra
 */
public class SM_GROUP_LOOT extends AionServerPacket {

	private final int groupId;
	private final int index;
	private final int itemCount;
	private final int itemId;
	private final int unk3;
	private final int lootCorpseId;
	private final int distributionId;
	private final int playerId;
	private final long luck;

	public SM_GROUP_LOOT(int groupId, int playerId, int itemId, int itemCount, int lootCorpseId, int distributionId, long luck, int index) {
		this.groupId = groupId;
		this.index = index;
		this.itemCount = itemCount;
		this.itemId = itemId;
		this.unk3 = 0;
		this.lootCorpseId = lootCorpseId;
		this.distributionId = distributionId;
		this.playerId = playerId;
		this.luck = luck;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(groupId);
		writeD(index);
		writeD(itemCount);
		writeD(itemId);
		writeC(unk3);
		writeC(0); // 3.0
		writeC(0); // 3.5
		writeD(lootCorpseId);
		writeC(distributionId);
		writeD(playerId); // 0 starts the roll option
		writeD((int) luck);
	}
}
