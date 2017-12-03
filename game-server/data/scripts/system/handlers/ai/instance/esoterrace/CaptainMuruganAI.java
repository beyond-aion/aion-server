package ai.instance.esoterrace;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("captain_murugan")
public class CaptainMuruganAI extends AggressiveNpcAI {

	private AtomicBoolean isAggred = new AtomicBoolean(false);
	private Future<?> task;
	private Future<?> specialSkillTask;

	public CaptainMuruganAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isAggred.compareAndSet(false, true)) {
			startTaskEvent();
		}
	}

	private void startTaskEvent() {
		VisibleObject target = getTarget();
		if (target instanceof Player) {
			SkillEngine.getInstance().getSkill(getOwner(), 19324, 10, target).useNoAnimationSkill();
		}
		task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead()) {
					cancelTask();
				} else {
					PacketSendUtility.broadcastMessage(getOwner(), 1500194);
					SkillEngine.getInstance().getSkill(getOwner(), 19325, 5, getOwner()).useNoAnimationSkill();
					if (getLifeStats().getHpPercentage() < 50) {
						specialSkillTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								if (!isDead()) {
									PacketSendUtility.broadcastMessage(getOwner(), 1500193);
									VisibleObject target = getTarget();
									if (target instanceof Player) {
										SkillEngine.getInstance().getSkill(getOwner(), 19324, 10, target).useNoAnimationSkill();
									}
									specialSkillTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

										@Override
										public void run() {
											if (!isDead()) {
												VisibleObject target = getTarget();
												if (target instanceof Player) {
													SkillEngine.getInstance().getSkill(getOwner(), 19324, 10, target).useNoAnimationSkill();
												}
											}

										}

									}, 4000);
								}

							}

						}, 10000);
					}
				}
			}

		}, 20000, 20000);

	}

	private void cancelTask() {
		if (task != null && !task.isDone()) {
			task.cancel(true);
		}
	}

	private void cancelSpecialSkillTask() {
		if (specialSkillTask != null && !specialSkillTask.isDone()) {
			specialSkillTask.cancel(true);
		}
	}

	@Override
	protected void handleBackHome() {
		cancelTask();
		cancelSpecialSkillTask();
		super.handleBackHome();
		isAggred.set(false);
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		cancelSpecialSkillTask();
		super.handleDespawned();
		isAggred.set(false);
	}

	@Override
	protected void handleDied() {
		cancelTask();
		cancelSpecialSkillTask();
		PacketSendUtility.broadcastMessage(getOwner(), 1500195);
		super.handleDied();
		isAggred.set(false);
	}

}
