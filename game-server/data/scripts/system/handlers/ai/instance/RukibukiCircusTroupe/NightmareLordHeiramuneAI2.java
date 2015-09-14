package ai.instance.RukibukiCircusTroupe;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 */
@AIName("nightmarelordheiramune")
public class NightmareLordHeiramuneAI2 extends AggressiveNpcAI2 {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> spawnTask;
	protected List<Integer> percents = new ArrayList<Integer>();

	@Override
	protected void handleSpawned() {
		addPercent();
		super.handleSpawned();
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, new Integer[] { 80, 50 });
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 80:
						startSpawnTask();
						break;
					case 50:
						spawn(233162, getOwner().getX() + 5, getOwner().getY() + 5, getOwner().getZ(), getOwner().getHeading());
						break;
				}
				break;
			}
		}
	}

	private void startSpawnTask() {
		spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isAlreadyDead())
					cancelTask();
				else {
					spawnHelpers();
				}
			}
		}, 0, 20000);
	}

	private void cancelTask() {
		if (spawnTask != null && !spawnTask.isCancelled()) {
			spawnTask.cancel(true);
		}
	}

	@Override
	protected void handleDied() {
		cancelTask();
		percents.clear();
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelTask();
		percents.clear();
		isHome.set(true);
	}

	private void spawnHelpers() {
		spawn(233457, 521.585f, 510.16528f, 199.59279f, (byte) 30);
		spawn(233457, 523.3747f, 621.1362f, 208.05113f, (byte) 90);
	}

}
