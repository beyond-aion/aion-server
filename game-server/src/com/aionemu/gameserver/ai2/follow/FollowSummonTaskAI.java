package com.aionemu.gameserver.ai2.follow;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.event.AIEventType;
import com.aionemu.gameserver.model.TaskId;
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
	private Future<?> task;

	public FollowSummonTaskAI(Creature target, Summon summon) {
		this.target = target;
		this.summon = summon;
		this.master = summon.getMaster();
		task = summon.getMaster().getController().getTask(TaskId.SUMMON_FOLLOW);
		setLeadingCoordinates();
	}

	private void setLeadingCoordinates() {
		targetX = target.getX();
		targetY = target.getY();
		targetZ = target.getZ();
	}

	@Override
	public void run() {
		if (target == null || summon == null || master == null) {
			if (task != null) {
				task.cancel(true);
			}
			return;
		}
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
		summon.getAi2().onCreatureEvent(AIEventType.ATTACK, target);
	}

	private void onOutOfTargetRange() {
		summon.getAi2().onGeneralEvent(AIEventType.MOVE_VALIDATE);
	}
}
