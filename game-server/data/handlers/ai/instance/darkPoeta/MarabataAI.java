package ai.instance.darkPoeta;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("marabata")
public class MarabataAI extends AggressiveNpcAI {

	private Future<?> boosterLifeCheckTask;
	private AtomicBoolean isStarted = new AtomicBoolean();

	public MarabataAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isStarted.compareAndSet(false, true)) {
			boosterLifeCheckTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
				if (isDead())
					return;
				switch (getNpcId()) {
					case 214849:
						if (!isBoosterAlive(700439))
							spawn(700439, 665.374f, 372.751f, 99.375f, (byte) 90);
						if (!isBoosterAlive(700440))
							spawn(700440, 681.851f, 408.625f, 100.472f, (byte) 13);
						if (!isBoosterAlive(700441))
							spawn(700441, 646.550f, 406.088f, 99.375f, (byte) 49);
						break;
					case 214850:
						if (!isBoosterAlive(700442))
							spawn(700442, 636.118f, 325.537f, 99.375f, (byte) 49);
						if (!isBoosterAlive(700443))
							spawn(700443, 676.257f, 319.650f, 99.375f, (byte) 4);
						if (!isBoosterAlive(700444))
							spawn(700444, 655.851f, 292.711f, 99.375f, (byte) 90);
						break;
					case 214851:
						if (!isBoosterAlive(700445))
							spawn(700445, 605.625f, 380.479f, 99.375f, (byte) 14);
						if (!isBoosterAlive(700446))
							spawn(700446, 598.706f, 345.978f, 99.375f, (byte) 98);
						if (!isBoosterAlive(700447))
							spawn(700447, 567.775f, 366.207f, 99.375f, (byte) 59);
						break;
				}
			}, 30000, 30000);
		}
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		switch (getNpcId()) {
			case 214849:
				spawn(700439, 665.374f, 372.751f, 99.375f, (byte) 90);
				spawn(700440, 681.851f, 408.625f, 100.472f, (byte) 13);
				spawn(700441, 646.550f, 406.088f, 99.375f, (byte) 49);
				break;
			case 214850:
				spawn(700442, 636.118f, 325.537f, 99.375f, (byte) 49);
				spawn(700443, 676.257f, 319.650f, 99.375f, (byte) 4);
				spawn(700444, 655.851f, 292.711f, 99.375f, (byte) 90);
				break;
			case 214851:
				spawn(700445, 605.625f, 380.479f, 99.375f, (byte) 14);
				spawn(700446, 598.706f, 345.978f, 99.375f, (byte) 98);
				spawn(700447, 567.775f, 366.207f, 99.375f, (byte) 59);
				break;
		}
	}

	private boolean isBoosterAlive(int boosterId) {
		return getPosition().getWorldMapInstance().getNpc(boosterId) != null;
	}

	private void cancelTask() {
		if (boosterLifeCheckTask != null && !boosterLifeCheckTask.isCancelled())
			boosterLifeCheckTask.cancel(true);
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelTask();
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}
}
