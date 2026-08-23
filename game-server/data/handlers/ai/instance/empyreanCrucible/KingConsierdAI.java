package ai.instance.empyreanCrucible;

import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, w4terbomb
 */
@AIName("king_consierd")
public class KingConsierdAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 75, 25);
	private Future<?> eventTask;
	private Future<?> skillTask;

	public KingConsierdAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDespawned() {
		cancelTasks();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelTasks();
		despawnNpcs(getPosition().getWorldMapInstance().getNpcs(282378));
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		cancelTasks();
		despawnNpcs(getPosition().getWorldMapInstance().getNpcs(282378));
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	private void scheduleInitialSkills() {
		ThreadPoolManager.getInstance().schedule(() -> {
			getOwner().queueSkill(19691, 1, 4000);
			getOwner().queueSkill(17954, 29);
		}, 2000);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 100 -> {
				startBloodThirstTask();
				scheduleInitialSkills();
			}
			case 75 -> startSkillTask();
			case 25 -> getOwner().queueSkill(19690, 1);
		}
	}

	private void startBloodThirstTask() {
		eventTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead()) {
				PacketSendUtility.broadcastToMap(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_IDArena_04());
				getOwner().queueSkill(19624, 10);
			}
		}, 180000);
	}

	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::executeSkillTask, 0, 25000);
	}

	private void executeSkillTask() {
		if (isDead()) {
			cancelTasks();
			return;
		}
		getOwner().queueSkill(17951, 29);
		ThreadPoolManager.getInstance().schedule(() -> {
			if (getLifeStats().getHpPercentage() <= 50)
				spawnBabyConsierd();
			ThreadPoolManager.getInstance().schedule(() -> getOwner().queueSkill(17952, 29), 2000);
		}, 3500);
	}

	private void spawnBabyConsierd() {
		var position = getPosition();
		spawn(282378, position.getX(), position.getY(), position.getZ(), position.getHeading());
		spawn(282378, position.getX(), position.getY(), position.getZ(), position.getHeading());
	}

	private void cancelTasks() {
		if (eventTask != null && !eventTask.isDone())
			eventTask.cancel(true);
		if (skillTask != null && !skillTask.isCancelled())
			skillTask.cancel(true);
	}

	private void despawnNpcs(List<Npc> npcs) {
		npcs.forEach(npc -> npc.getController().delete());
	}
}
