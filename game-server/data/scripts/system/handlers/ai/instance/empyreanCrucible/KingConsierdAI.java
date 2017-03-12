package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.controllers.attack.AggroInfo;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Luzien
 */
@AIName("king_consierd")
public class KingConsierdAI extends AggressiveNpcAI {

	private List<Integer> percents = new ArrayList<>();
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> eventTask;
	private Future<?> skillTask;

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		addPercents();
	}

	@Override
	public void handleDespawned() {
		cancelTasks();
		percents.clear();
		super.handleDespawned();
	}

	@Override
	public void handleDied() {
		cancelTasks();
		despawnNpcs(getPosition().getWorldMapInstance().getNpcs(282378));
		super.handleDied();
	}

	@Override
	public void handleBackHome() {
		cancelTasks();
		addPercents();
		despawnNpcs(getPosition().getWorldMapInstance().getNpcs(282378));
		super.handleBackHome();
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
		if (isHome.compareAndSet(true, false)) {
			startBloodThirstTask();

			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					SkillEngine.getInstance().getSkill(getOwner(), 19691, 1, getTarget()).useNoAnimationSkill();
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							SkillEngine.getInstance().getSkill(getOwner(), 17954, 29, getTarget()).useNoAnimationSkill();
						}

					}, 4000);

				}
			}, 2000);
		}
	}

	private void startBloodThirstTask() {
		eventTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				SkillEngine.getInstance().getSkill(getOwner(), 19624, 10, getOwner()).useNoAnimationSkill();

			}
		}, 180 * 1000); // 3min, need confirm
	}

	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead()) {
					cancelTasks();
				} else {
					SkillEngine.getInstance().getSkill(getOwner(), 17951, 29, getTarget()).useNoAnimationSkill();
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							dropAggro();

							if (getLifeStats().getHpPercentage() <= 50) {
								WorldPosition p = getPosition();
								spawn(282378, p.getX(), p.getY(), p.getZ(), p.getHeading());
								spawn(282378, p.getX(), p.getY(), p.getZ(), p.getHeading());
							}
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									SkillEngine.getInstance().getSkill(getOwner(), 17952, 29, getTarget()).useNoAnimationSkill();
								}

							}, 2000);
						}

					}, 3500);
				}
			}
		}, 0, 25000);
	}

	private void dropAggro() {
		if (getTarget() instanceof Creature) {
			Creature hated = (Creature) getTarget();
			if (getAggroList().isHating(hated)) {
				AggroInfo ai = getAggroList().getAggroInfo(hated);
				ai.setHate(ai.getHate() / 2);
				think();
			}
		}
	}

	private void cancelTasks() {
		if (eventTask != null && !eventTask.isDone()) {
			eventTask.cancel(true);
		}
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	private void checkPercentage(int percentage) {
		for (Integer percent : percents) {
			if (percentage <= percent) {
				percents.remove(percent);
				if (percent == 75) {
					startSkillTask();
				} else if (percent == 25) {
					SkillEngine.getInstance().getSkill(getOwner(), 19690, 1, getTarget()).useNoAnimationSkill();
				}
				break;
			}
		}
	}

	private void addPercents() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 75, 25 });
	}

	private void despawnNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			npc.getController().delete();
		}
	}
}
