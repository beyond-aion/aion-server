package ai.instance.beshmundirTemple;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.GeneralNpcAI;

/**
 * @author Tibald
 */
@AIName("aethic_field_generator")
public class AethicFieldGeneratorAI extends GeneralNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 50, 25);
	private AtomicBoolean isAggred = new AtomicBoolean(false);
	private Future<?> aggroTask;

	public AethicFieldGeneratorAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 19127, 1, getOwner()).useNoAnimationSkill();
	}

	@Override
	protected void handleAttack(Creature creature) {
		if (isAggred.compareAndSet(false, true)) {
			getPosition().getWorldMapInstance().setDoorState(471, false);
			aggroTask();
		}
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 75:
				spawn(216532, 1364.3739f, 88.38297f, 248.39449f, (byte) 37);
				spawn(216532, 1349.2109f, 87.81685f, 248.37459f, (byte) 5);
				break;
			case 50:
				spawn(216532, 1362.2529f, 88.02893f, 248.40222f, (byte) 53);
				spawn(216532, 1352.1556f, 87.69477f, 248.38733f, (byte) 4);
				spawn(216532, 1356.2352f, 90.32507f, 247.75319f, (byte) 92);
				break;
			case 25:
				spawn(216532, 1366.1492f, 84.480515f, 248.59457f, (byte) 51);
				spawn(216532, 1349.3264f, 83.13163f, 248.59457f, (byte) 6);
				spawn(216532, 1354.3655f, 85.42517f, 248.59457f, (byte) 27);
				spawn(216532, 1360.4175f, 86.17866f, 248.59457f, (byte) 34);
				break;
		}
	}

	private void aggroTask() {
		aggroTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (isDead()) {
				cancelAggroTask();
			} else {
				if (!isInRangePlayer()) {
					handleBackHome();
					getOwner().getController().loseAggro(true);
				}
			}
		}, 2000, 2000);
	}

	private boolean isInRangePlayer() {
		for (Player player : getKnownList().getKnownPlayers().values()) {
			if (isInRange(player, 40) && !player.isDead() && getOwner().canSee(player)) {
				return true;
			}
		}
		return false;
	}

	private void cancelAggroTask() {
		if (aggroTask != null && !aggroTask.isDone()) {
			aggroTask.cancel(true);
		}
	}

	@Override
	protected void handleBackHome() {
		handleFinishAttack();
		cancelAggroTask();
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null) {
			instance.setDoorState(471, true);
			deleteNpcs(instance.getNpcs(216532));
		}
		isAggred.set(false);
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleDied() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		if (instance != null) {
			instance.setDoorState(471, true);
			deleteNpcs(instance.getNpcs(216532));
		}
		cancelAggroTask();
		super.handleDied();

	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().delete();
			}
		}
	}
}
