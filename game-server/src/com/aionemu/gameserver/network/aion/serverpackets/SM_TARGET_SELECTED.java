package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr, -Enomine-
 */
public class SM_TARGET_SELECTED extends AionServerPacket {

	private int targetObjId;
	private int level;
	private int maxHp, currentHp;
	private int maxMp, currentMp;

	public SM_TARGET_SELECTED(VisibleObject target) {
		if (target != null) {
			this.targetObjId = target.getObjectId();
			if (target instanceof Creature) {
				Creature creature = (Creature) target;
				this.level = creature.getLevel();
				this.maxHp = creature.getLifeStats().getMaxHp();
				this.currentHp = creature.getLifeStats().getCurrentHp();
				this.maxMp = creature.getLifeStats().getMaxMp();
				this.currentMp = creature.getLifeStats().getCurrentMp();
			}
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjId);
		writeH(level);
		writeD(maxHp);
		writeD(currentHp);
		writeD(maxMp);// new 4.0
		writeD(currentMp);// new 4.0
	}

}
