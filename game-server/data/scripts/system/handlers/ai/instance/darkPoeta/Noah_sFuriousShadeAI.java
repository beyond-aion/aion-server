package ai.instance.darkPoeta;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;
import javolution.util.FastTable;

/**
 * @author Ritsu
 */
@AIName("spectral_tree")
public class Noah_sFuriousShadeAI extends AggressiveNpcAI {

	private Future<?> skillTask;
	private Future<?> skill2Task;
	protected List<Integer> percents = new FastTable<>();

	@Override
	public void handleSpawned() {
		super.handleSpawned();
		addPercent();
		useSkill();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 30:
						AIActions.useSkill(this, 18529);
						useSkillTree();
						break;
				}
				break;
			}
		}
	}

	private void useSkill() {
		if (Rnd.get(2) > 0) {
			SkillEngine.getInstance().getSkill(getOwner(), 16822, 50, getOwner()).useSkill();
		}
	}

	private void useSkillTree() {
		skillTask = ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				SkillEngine.getInstance().getSkill(getOwner(), 17736, 50, getTarget()).useSkill();
				skill2Task = ThreadPoolManager.getInstance().schedule(new Runnable() {

					@Override
					public void run() {
						SkillEngine.getInstance().getSkill(getOwner(), 18531, 50, getTarget()).useSkill();
					}
				}, 8000);
			}
		}, 7000);
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 30 });
	}

	private void cancelTask() {
		if (skillTask != null && !skillTask.isDone()) {
			skillTask.cancel(true);
		} else if (skill2Task != null && !skill2Task.isDone()) {
			skill2Task.cancel(true);
		}
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		cancelTask();
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		percents.clear();
		cancelTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		percents.clear();
		cancelTask();
		super.handleDied();
	}
}
