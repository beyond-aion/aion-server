package ai.instance.dragonLordsRefuge;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI2;
import javolution.util.FastTable;

/**
 * @author Cheatkiller
 * @reworked Luzien
 */
@AIName("tiamat")
public class TiamatAI2 extends AggressiveNpcAI2 {

	protected List<Integer> percents = new FastTable<>();
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private AtomicBoolean isSinkingFlag = new AtomicBoolean(false);
	private Future<?> skillTask;
	private Future<?> painTask;
	private Future<?> addTask;
	private Future<?> sinkingTask;
	private int variable = 95;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			startSkillTask();
			startSlickTask();
		}
		checkPercentage(getLifeStats().getHpPercentage());
	}

	@Override
	protected void handleAttackComplete() {
		super.handleAttackComplete();
		if (isSinkingFlag.get() == true) {
			spawnSinkingSand(variable += 5);
			if (variable == 130) {
				variable = 0;
			}
			if (variable == 20) {
				isSinkingFlag.set(false);
				variable = 95;
			}
		}
	}

	private void startSlickTask() {
		sinkingTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelSinkTask();
				else {
					isSinkingFlag.set(true);
				}
			}
		}, 10000, 50000);
	}

	private void startPainTask() {
		painTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelTasks();
				else {
					spawnInfinitePain();
				}
			}
		}, 80000, 80000);
	}

	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelTasks();
				else {
					atrocityEvent();
				}
			}
		}, 10000, 25000);
	}

	private void cancelTasks() {
		cancelSkillTask();
		if (addTask != null && !addTask.isCancelled()) {
			addTask.cancel(true);
		}
		if (painTask != null && !painTask.isCancelled()) {
			painTask.cancel(true);
		}
		if (sinkingTask != null && !sinkingTask.isCancelled()) {
			sinkingTask.cancel(true);
		}
	}

	private void cancelSkillTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	private void cancelSinkTask() {
		if (sinkingTask != null && !sinkingTask.isCancelled()) {
			sinkingTask.cancel(true);
		}
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 50:
						cancelSkillTask();
						spawnDivisiveCreation();
						break;
					case 25:
						startPainTask();
						spawnGravityCrusher();
						break;
					case 15:
					case 5:
						spawnGravityCrusher();
						break;
				}

				break;
			}
		}
	}

	/*
	 * sinking sand 283330 skill -> 20965 TODO
	 */
	private void atrocityEvent() {
		int var = Rnd.get(3);
		int skill = 20922 + (var * 2); // 20922/20924/20926, left,central,right
		spawnAtrocityNPCs(var);
		SkillEngine.getInstance().getSkill(getOwner(), skill, 60, getOwner()).useNoAnimationSkill(); // Animation without damage
	}

	private void spawnAtrocityNPCs(int var) {
		switch (var) {
			case 0:
				spawn(283237, 445.0000f, 550.7000f, 417.4000f, (byte) 0);
				break;
			case 2:
				spawn(283244, 454.1000f, 474.9000f, 417.4000f, (byte) 0);
				break;
			case 1:
				spawn(283240, 457.8000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 462.3000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 469.7000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 466.6000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 473.6000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 479.3000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 475.8000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 491.2000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 482.7000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 485.2000f, 514.6000f, 417.4000f, (byte) 0);
				spawn(283240, 488.1000f, 514.6000f, 417.4000f, (byte) 0);
				break;
		}
	}

	// 105, 110, 115, 120, 125, 5, 10, 20
	private void spawnSinkingSand(float heading) {
		double radian = Math.toRadians(MathUtil.convertHeadingToDegree((byte) heading));
		int dist = 5;
		for (int i = 0; i < 10; i++) {
			dist += 3;
			float x = (float) (Math.cos(radian) * dist);
			float y = (float) (Math.sin(radian) * dist);
			spawn(283135, getPosition().getX() + x, getPosition().getY() + y, getPosition().getZ(), (byte) 0);
			spawn(283136, getPosition().getX() + x, getPosition().getY() + y, getPosition().getZ(), (byte) 0);
		}
	}

	private void spawnDivisiveCreation() {
		addTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelTasks();
				else {
					spawn(283139, 464.24f, 462.26f, 417.4f, (byte) 18);
					spawn(283139, 542.79f, 465.03f, 417.4f, (byte) 43);
					spawn(283139, 541.79f, 563.71f, 417.4f, (byte) 74);
					spawn(283139, 465.79f, 565.43f, 417.4f, (byte) 100);
				}
			}
		}, 70000, 40000);
	}

	private void spawnGravityCrusher() {
		spawn(283141, 464.24f, 462.26f, 417.4f, (byte) 18);
		spawn(283141, 542.79f, 465.03f, 417.4f, (byte) 43);
		spawn(283141, 541.79f, 563.71f, 417.4f, (byte) 74);
		spawn(283141, 465.79f, 565.43f, 417.4f, (byte) 100);
	}

	private void spawnInfinitePain() {
		spawn(283143, 508.32f, 515.18f, 417.4f, (byte) 0);
		spawn(283144, 508.32f, 515.18f, 417.4f, (byte) 0);
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 50, 25, 15, 5 });
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		percents.clear();
		despawnAdds();
		cancelTasks();
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		super.handleBackHome();
		despawnAdds();
		cancelTasks();
		isHome.set(true);
		isSinkingFlag.set(false);
	}

	private void despawnAdds() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(283141));
		deleteNpcs(instance.getNpcs(283139));
		deleteNpcs(instance.getNpcs(283140));
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().delete();
			}
		}
	}

	@Override
	protected void handleDied() {
		percents.clear();
		despawnAdds();
		super.handleDied();
		cancelTasks();
	}
}
