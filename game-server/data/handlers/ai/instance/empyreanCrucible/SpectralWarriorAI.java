package ai.instance.empyreanCrucible;

import java.util.concurrent.ScheduledFuture;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, w4terbomb
 */
@AIName("spectral_warrior")
public class SpectralWarriorAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50);
	private ScheduledFuture<?> replaceSpawnsTask;

	public SpectralWarriorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDespawned() {
		cancelTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelTask();
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		cancelTask();
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		getPosition().getWorldMapInstance().getInstanceHandler().onChangeStage(StageType.START_STAGE_6_ROUND_5);
		replaceSpawnsTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::resurrectAllies, 0, 2000);
	}

	private void resurrectAllies() {
		getKnownList().forEachNpc(npc -> {
			if (npc.isDead())
				return;
			switch (npc.getNpcId()) {
				case 205413 -> {
					spawn(217576, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
					npc.getController().delete();
				}
				case 205414 -> {
					spawn(217577, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
					npc.getController().delete();
				}
			}
		});
	}

	private void cancelTask() {
		if (replaceSpawnsTask != null && !replaceSpawnsTask.isDone()) {
			replaceSpawnsTask.cancel(true);
		}
	}
}
