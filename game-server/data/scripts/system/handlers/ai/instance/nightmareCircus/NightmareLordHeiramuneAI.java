package ai.instance.nightmareCircus;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("nightmarelordheiramune")
public class NightmareLordHeiramuneAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> spawnTask;
	protected List<Integer> percents = new ArrayList<>();

	public NightmareLordHeiramuneAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	private synchronized void addPercent() {
		percents.clear();
		Collections.addAll(percents, 80, 50);
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
					case 80 -> startSpawnTask();
					case 50 -> {
						PacketSendUtility.broadcastMessage(getOwner(), 1501138);
						spawn(233162, getOwner().getX() + 5, getOwner().getY() + 5, getOwner().getZ(), getOwner().getHeading());
					}
				}
				break;
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
		addPercent();
		isHome.set(true);
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
