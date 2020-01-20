package ai.events;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Sykra
 */
@AIName("world_raid_aggressive")
public class WorldRaidAI extends AggressiveNpcAI {

	private final AtomicBoolean isEffectNpcSpawned = new AtomicBoolean();
	private Npc effectNpc;
	private ScheduledFuture<?> effectNpcDespawnTask;

	public WorldRaidAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		if (isEffectNpcSpawned.compareAndSet(false, true)) {
			// spawn npc 702549 (WorldRaid_Advent_Effect)
			effectNpc = (Npc) spawn(702549, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);
			effectNpcDespawnTask = ThreadPoolManager.getInstance().schedule(() -> {
				if (effectNpc != null) {
					effectNpc.getController().delete();
					effectNpc = null;
				}
				effectNpcDespawnTask = null;
			}, 10000);
		}
		super.handleAttack(creature);
	}

	@Override
	protected void handleBackHome() {
		cancelDespawnTaskAndDespawnEffectNpc();
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		cancelDespawnTaskAndDespawnEffectNpc();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelDespawnTaskAndDespawnEffectNpc();
		super.handleDied();
	}

	private void cancelDespawnTaskAndDespawnEffectNpc() {
		if (effectNpcDespawnTask != null && !effectNpcDespawnTask.isCancelled()) {
			effectNpcDespawnTask.cancel(true);
			effectNpcDespawnTask = null;
		}
		if (effectNpc != null) {
			effectNpc.getController().delete();
			effectNpc = null;
		}
		isEffectNpcSpawned.set(false);
	}

}
