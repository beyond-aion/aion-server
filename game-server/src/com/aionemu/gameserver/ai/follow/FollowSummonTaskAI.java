package com.aionemu.gameserver.ai.follow;

import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.model.summons.UnsummonType;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.utils.MathUtil;

/**
 * @author xTz
 */
public class FollowSummonTaskAI implements Runnable {

	private Creature target;
	private Summon summon;
	private Player master;
	private float targetX;
	private float targetY;
	private float targetZ;

	public FollowSummonTaskAI(Creature target, Summon summon) {
		this.target = target;
		this.summon = summon;
		this.master = summon.getMaster();
		setLeadingCoordinates();
	}

	private void setLeadingCoordinates() {
		targetX = target.getX();
		targetY = target.getY();
		targetZ = target.getZ();
	}

	@Override
	public void run() {
		if (!isInMasterRange()) {
			SummonsService.doMode(SummonMode.RELEASE, summon, UnsummonType.DISTANCE);
			return;
		}
		if (!isInTargetRange()) {
			if (targetX != target.getX() || targetY != target.getY() || targetZ != target.getZ()) {
				setLeadingCoordinates();
				onOutOfTargetRange();
			}
		} else if (!master.equals(target)) {
			onDestination();
		}
	}

	private boolean isInTargetRange() {
		return MathUtil.isIn3dRange(target, summon, 2);
	}

	private boolean isInMasterRange() {
		return MathUtil.isIn3dRange(master, summon, 50);
	}

	protected void onDestination() {
		summon.getAi().onCreatureEvent(AIEventType.ATTACK, target);
	}

	private void onOutOfTargetRange() {
		summon.getAi().onGeneralEvent(AIEventType.MOVE_VALIDATE);
	}
}
