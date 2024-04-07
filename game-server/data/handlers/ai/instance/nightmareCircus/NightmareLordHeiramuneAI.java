package ai.instance.nightmareCircus;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("nightmarelordheiramune")
public class NightmareLordHeiramuneAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(80, 50);
	private Future<?> spawnTask;

	public NightmareLordHeiramuneAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 80 -> startSpawnTask();
			case 50 -> {
				PacketSendUtility.broadcastMessage(getOwner(), 1501138);
				spawn(233162, getOwner().getX() + 5, getOwner().getY() + 5, getOwner().getZ(), getOwner().getHeading());
			}
		}
	}

	private void startSpawnTask() {
		spawnTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead()) {
				spawnHelpers();
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
		super.handleDied();
		cancelTask();
		despawnNpcs(233457, 233162);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelTask();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		cancelTask();
		hpPhases.reset();
	}

	private void despawnNpcs(int... npcIds) {
		for (Npc npc : getOwner().getPosition().getWorldMapInstance().getNpcs(npcIds)) {
			npc.getController().delete();
		}
	}

	private void spawnHelpers() {
		PacketSendUtility.broadcastMessage(getOwner(), 1501139);
		spawn(233457, 521.585f, 510.16528f, 199.59279f, (byte) 30);
		spawn(233457, 523.3747f, 621.1362f, 208.05113f, (byte) 90);
	}

}
