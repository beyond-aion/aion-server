package ai.instance.empyreanCrucible;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import javolution.util.FastTable;
import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Luzien
 */
@AIName("alukina_emp")
public class QueenAlukinaAI2 extends AggressiveNpcAI2 {

	private List<Integer> percents = new FastTable<Integer>();
	private Future<?> task;

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		addPercents();
	}

	@Override
	public void handleDespawned() {
		cancelTask();
		percents.clear();
		super.handleDespawned();
	}

	@Override
	public void handleDied() {
		cancelTask();
		super.handleDied();
	}

	@Override
	public void handleBackHome() {
		addPercents();
		cancelTask();
		super.handleBackHome();
	}

	@Override
	public void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void startEvent(int percent) {

		SkillEngine.getInstance().getSkill(getOwner(), 17899, 41, getTarget()).useNoAnimationSkill();

		switch (percent) {
			case 75:
				scheduleSkill(17900, 4500);
				NpcShoutsService.getInstance().sendMsg(getOwner(), 340487, getObjectId(), 0, 10000);
				scheduleSkill(17899, 14000);
				scheduleSkill(17900, 18000);
				break;
			case 50:
				scheduleSkill(17280, 4500);
				scheduleSkill(17902, 8000);
				break;
			case 25:
				task = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

					@Override
					public void run() {
						if (isAlreadyDead()) {
							cancelTask();
						} else {
							SkillEngine.getInstance().getSkill(getOwner(), 17901, 41, getTarget()).useNoAnimationSkill();
							scheduleSkill(17902, 5500);
							scheduleSkill(17902, 7500);
						}
					}
				}, 4500, 20000);
				break;
		}
	}

	private void cancelTask() {
		if (task != null && !task.isCancelled())
			task.cancel(true);
	}

	private void scheduleSkill(final int skill, int delay) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				if (!isAlreadyDead()) {
					SkillEngine.getInstance().getSkill(getOwner(), skill, 41, getTarget()).useNoAnimationSkill();

				}
			}
		}, delay);
	}

	private void checkPercentage(int percentage) {
		for (Integer percent : percents) {
			if (percentage <= percent) {
				percents.remove(percent);
				startEvent(percent);
				break;
			}
		}
	}

	private void addPercents() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 75, 50, 25 });
	}
}
