package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr
 * @modified -Enomine- 4.0
 */
public class SM_TARGET_SELECTED extends AionServerPacket {

	private int level;
	private int maxHp;
	private int currentHp;
	private int maxMp;
	private int currentMp;
	private int targetObjId;

	public SM_TARGET_SELECTED(Player player) {
		if (player != null) {
			if (player.getTarget() instanceof Creature) {
				Creature target = (Creature) player.getTarget();
				this.level = target.getLevel();
				this.maxHp = target.getLifeStats().getMaxHp();
				this.currentHp = target.getLifeStats().getCurrentHp();
				this.maxMp = target.getLifeStats().getMaxMp();
				this.currentMp = target.getLifeStats().getCurrentMp();
			}
			else {
				// TODO: check various gather on retail
				this.level = 0;
				this.maxHp = 0;
				this.currentHp = 0;
				this.maxMp = 0;
				this.currentMp = 0;
			}

			if (player.getTarget() != null)
				targetObjId = player.getTarget().getObjectId();
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjId);
		writeH(level);
		writeD(maxHp);
		writeD(currentHp);
		writeD(maxMp);//new 4.0
		writeD(currentMp);//new 4.0
	}

}