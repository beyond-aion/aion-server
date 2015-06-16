package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Rhys2002
 */
public class SM_GROUP_LOOT extends AionServerPacket {

	private int groupId;
	private int index;
	private int unk2;
	private int itemId;
	private int unk3;
	private int unk4;
	private int lootCorpseId;
	private int distributionId;
	private int playerId;
	private long luck;

	/**
	 * @param Player
	 *          Id must be 0 to start the Roll Options
	 */
	public SM_GROUP_LOOT(int groupId, int playerId, int itemId, int lootCorpseId, int distributionId, long luck, int index) {
		this.groupId = groupId;
		this.index = index;
		this.unk2 = 1;
		this.itemId = itemId;
		this.unk3 = 0;
		this.lootCorpseId = lootCorpseId;
		this.distributionId = distributionId;
		this.playerId = playerId;
		this.luck = luck;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(groupId);
		writeD(index);
		writeD(unk2);
		writeD(itemId);
		writeC(unk3);
		writeC(0); // 3.0
		writeC(0); // 3.5
		writeD(lootCorpseId);
		writeC(distributionId);
		writeD(playerId);
		writeD((int)luck);
	}
}
