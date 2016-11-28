package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 */
public class SM_SKILL_CANCEL extends AionServerPacket {

	private Creature creature;
	private int skillId;

	public SM_SKILL_CANCEL(Creature creature, int skillId) {
		this.creature = creature;
		this.skillId = skillId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(creature.getObjectId());
		writeH(skillId);
	}
}
