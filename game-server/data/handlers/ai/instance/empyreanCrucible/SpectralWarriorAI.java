package ai.instance.empyreanCrucible;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.instance.StageType;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, w4terbomb
 */
@AIName("spectral_warrior")
public class SpectralWarriorAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(50);
	private Future<?> attackTask;

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
		getOwner().queueSkill(19546, 10);
		List<Npc> raised = resurrectAllies();
		attackTask = ThreadPoolManager.getInstance().schedule(() -> {
			if (isDead() || !(getTarget() instanceof Creature target))
				return;
			raised.stream().filter(npc -> !npc.isDead()).forEach(npc -> npc.getAggroList().addHate(target, 1000));
		}, 10000);
	}

	private List<Npc> resurrectAllies() {
		List<Npc> raised = new ArrayList<>();
		getKnownList().forEachNpc(npc -> {
			if (npc.isDead())
				return;
			int raisedNpcId = switch (npc.getNpcId()) {
				case 205413 -> 217576;
				case 205414 -> 217577;
				default -> 0;
			};
			if (raisedNpcId == 0)
				return;
			VisibleObject spawned = spawn(raisedNpcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading());
			npc.getController().delete();
			if (spawned instanceof Npc raisedNpc)
				raised.add(raisedNpc);
		});
		return raised;
	}

	private void cancelTask() {
		if (attackTask != null && !attackTask.isDone())
			attackTask.cancel(true);
	}
}
