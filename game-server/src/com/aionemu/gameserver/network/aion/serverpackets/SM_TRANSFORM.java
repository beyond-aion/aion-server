package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.BoundRadius;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sweetkr, xTz, kecimis
 */
public class SM_TRANSFORM extends AionServerPacket {

	private final Creature creature;

	public SM_TRANSFORM(Creature creature) {
		this.creature = creature;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(creature.getObjectId());
		writeD(creature.getTransformModel().getModelId());
		writeH(creature.getState());
		// Players keep their fixed cylinder; an NPC sends its own template bounds (or the client default).
		BoundRadius bounds = creature instanceof Player player ? player.getCommonData().getBoundRadius()
			: creature.getObjectTemplate().getBoundRadius();
		writeF(bounds.getMaxOfFrontAndSide());
		writeF(bounds.getUpper());
		writeC(creature.getTransformModel().cantUseSkills() ? 1 : 0);
		writeD(creature.getTransformModel().getType().getId());
		writeC(creature.getTransformModel().cantFly() ? 1 : 0);
		writeC(creature.getTransformModel().cantUseItems() ? 1 : 0);
		writeC(creature.getTransformModel().cantAttack() ? 1 : 0);
		writeC(creature.getTransformModel().cantJump() ? 1 : 0);
		writeC(creature.getTransformModel().cantRecall() ? 1 : 0);
		writeC(creature.getTransformModel().cantMove() ? 1 : 0);
		writeD(creature.getTransformModel().getPanelId()); // display panel
	}
}
