package ai.instance.abyssal_splinter;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu, Luzien
 */
@AIName("yamennes")
public class YamenesAI extends AggressiveNpcAI {

	private boolean top;
	private final List<Integer> percents = new ArrayList<>();
	private Future<?> portalTask = null;
	private final AtomicBoolean isStart = new AtomicBoolean(false);

	public YamenesAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		addPercent();
		top = true;
		PacketSendUtility.broadcastToMap(getOwner(), 1400732);
		super.handleSpawned();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isStart.compareAndSet(false, true)) {
			startTasks();
		}
	}

	private void startTasks() {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				EmoteManager.emoteStopAttacking(getOwner());
				SkillEngine.getInstance().getSkill(getOwner(), 19098, 55, getOwner()).useSkill();
			}
		}, 600000);

		portalTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isDead()) {
				cancelTask();
			} else {
				spawnPortal();
				ThreadPoolManager.getInstance().schedule(() -> {
					WorldMapInstance instance = getPosition().getWorldMapInstance();
					deleteNpcs(instance.getNpcs(282107));
					Npc boss = getOwner();
					EmoteManager.emoteStopAttacking(getOwner());
					SkillEngine.getInstance().getSkill(boss, 19282, 55, getTarget()).useSkill();
					spawn(282107, boss.getX() + 10, boss.getY() - 10, boss.getZ(), (byte) 0);
					spawn(282107, boss.getX() - 10, boss.getY() + 10, boss.getZ(), (byte) 0);
					spawn(282107, boss.getX() + 10, boss.getY() + 10, boss.getZ(), (byte) 0);
					boss.clearAttackedCount();
					PacketSendUtility.broadcastToMap(getOwner(), 1400729);
				}, 3000);
			}
		}, 60000, 60000);
	}

	private void spawnPortal() {
		Npc portalA = getPosition().getWorldMapInstance().getNpc(282014);
		Npc portalB = getPosition().getWorldMapInstance().getNpc(282015);
		Npc portalC = getPosition().getWorldMapInstance().getNpc(282131);
		if (portalA == null && portalB == null && portalC == null) {
			if (!top) {
				PacketSendUtility.broadcastToMap(getOwner(), 1400637);
				spawn(282014, 288.10f, 741.95f, 216.81f, (byte) 3);
				spawn(282015, 375.05f, 750.67f, 216.82f, (byte) 59);
				spawn(282131, 341.33f, 699.38f, 216.86f, (byte) 59);
				top = true;
			} else {
				PacketSendUtility.broadcastToMap(getOwner(), 1400637);
				spawn(282014, 303.69f, 736.35f, 198.7f, (byte) 0);
				spawn(282015, 335.19f, 708.92f, 198.9f, (byte) 35);
				spawn(282131, 360.23f, 741.07f, 198.7f, (byte) 0);
				top = false;
			}
		}
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null)
				npc.getController().delete();
		}
	}

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, 100);
	}

	private void cancelTask() {
		if (portalTask != null && !portalTask.isDone()) {
			portalTask.cancel(true);
		}
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		top = true;
		cancelTask();
		isStart.set(false);
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		percents.clear();
		cancelTask();
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(282107));
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		percents.clear();
		cancelTask();
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(282107));
		super.handleDied();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case SHOULD_LOOT, SHOULD_REWARD_AP -> false;
			default -> super.ask(question);
		};
	}
}
