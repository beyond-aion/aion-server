package ai.instance.empyreanCrucible;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
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
public class KingConsierdAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 25);
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> eventTask;
	private Future<?> skillTask;

	public KingConsierdAI(Npc owner) {
		super(owner);
	}

	@Override
	public void handleDespawned() {
		cancelTasks();
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
		despawnNpcs(getPosition().getWorldMapInstance().getNpcs(282378));
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
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

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 75 -> startSkillTask();
			case 25 -> SkillEngine.getInstance().getSkill(getOwner(), 19690, 1, getTarget()).useNoAnimationSkill();
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
				if (isDead()) {
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

	private void despawnNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			npc.getController().delete();
		}
	}
}
