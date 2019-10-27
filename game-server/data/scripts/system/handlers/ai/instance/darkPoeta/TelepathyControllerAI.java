package ai.instance.darkPoeta;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 * @reworked Estrayl 12.06.2017
 */
@AIName("telepathy_controller")
public class TelepathyControllerAI extends AggressiveNpcAI {

	private Future<?> spawnTask;
	private final AtomicBoolean isAggred = new AtomicBoolean();

	public TelepathyControllerAI(Npc owner) {
		super(owner);
	}

	private Npc spawnHelper() {
		int distance = Rnd.get(7, 10);
		float direction = Rnd.get(200) / 100f;
		float xOff = (float) (Math.cos(Math.PI * direction) * distance);
		float yOff = (float) (Math.sin(Math.PI * direction) * distance);
		return (Npc) spawn(Rnd.get(2) == 0 ? 281150 : 281334, getPosition().getX() + xOff, getPosition().getY() + yOff, getPosition().getZ(), (byte) 0);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isAggred.compareAndSet(false, true)) {
			synchronized (isAggred) {
				if (isAggred.get()) {
					spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
						if (!isDead()) {
							Npc spawn = spawnHelper();
							spawn.getKnownList().forEachPlayer(p -> spawn.getAggroList().addHate(p, 10));
						}
					}, 60000, 60000);
				}
			}
		}
	}

	private void cancelTask() {
		if (spawnTask != null && !spawnTask.isCancelled())
			spawnTask.cancel(true);
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		synchronized (isAggred) {
			cancelTask();
			isAggred.set(false);
		}
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
