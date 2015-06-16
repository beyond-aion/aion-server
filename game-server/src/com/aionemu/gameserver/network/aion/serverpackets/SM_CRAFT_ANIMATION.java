package com.aionemu.gameserver.network.aion.serverpackets;


import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Mr. Poke
 */
public class SM_CRAFT_ANIMATION extends AionServerPacket {

	private int senderObjectId;
	private int targetObjectId;
	private int skillId;
	private int action;

	/**
	 * @param senderObjectId
	 * @param targetObjectId
	 * @param skillId
	 * @param action
	 */
	public SM_CRAFT_ANIMATION(int senderObjectId, int targetObjectId, int skillId, int action) {
		this.senderObjectId = senderObjectId;
		this.targetObjectId = targetObjectId;
		this.skillId = skillId;
		this.action = action;
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(senderObjectId);
		writeD(targetObjectId);
		writeH(skillId);
		writeC(action);
	}
}
