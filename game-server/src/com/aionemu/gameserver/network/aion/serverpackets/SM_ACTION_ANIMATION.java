package com.aionemu.gameserver.network.aion.serverpackets;

import com.aionemu.gameserver.model.animations.ActionAnimation;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer
 */
public class SM_ACTION_ANIMATION extends AionServerPacket {

	private int targetObjectId;
	private ActionAnimation actionAnimation;
	private int levelOrObjectId;

	public SM_ACTION_ANIMATION(int targetObjectId, ActionAnimation actionAnimation) {
		this(targetObjectId, actionAnimation, 0);
	}

	public SM_ACTION_ANIMATION(int targetObjectId, ActionAnimation actionAnimation, int levelOrObjectId) {
		this.targetObjectId = targetObjectId;
		this.actionAnimation = actionAnimation;
		this.levelOrObjectId = levelOrObjectId;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeD(targetObjectId);
		writeH(actionAnimation.getId());
		writeD(levelOrObjectId);
	}
}
