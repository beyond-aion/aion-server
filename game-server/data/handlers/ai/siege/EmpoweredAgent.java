package ai.siege;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("empowered_agent")
public class EmpoweredAgent extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	public EmpoweredAgent(Npc owner) {
		super(owner);
	}

	private final List<Integer> guardIds = new ArrayList<>();
	private final HpPhases hpPhases = new HpPhases(80, 70, 60, 50, 40, 30, 25, 20, 5);
	private final AtomicBoolean isWalkingCompleted = new AtomicBoolean();
	private boolean canThink = true;
	private Npc flagNpc;
	private Future<?> activationTask, aggroResetTask;

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 21779, 1, getOwner()).useNoAnimationSkill();
		canThink = false;
		EmoteManager.emoteStopAttacking(getOwner());
		switch (getOwner().getNpcId()) {
			case 235064 -> {
				Collections.addAll(guardIds, 235334, 235335, 235336, 235337, 235338, 235339);
				startWalking("600100000_npcpathgod_l");
			}
			case 235065 -> {
				Collections.addAll(guardIds, 235340, 235341, 235342, 235343, 235344, 235345);
				startWalking("600100000_npcpathgod_d");
			}
		}
	}

	private void startWalking(String walkerId) {
		getOwner().getSpawn().setWalkerId(walkerId);
		WalkManager.startWalking(this);
		getOwner().setState(CreatureState.WALK_MODE);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 80, 70, 60, 50, 40, 30, 20 -> onGuardSpawnEvent();
			case 25, 5 -> getOwner().queueSkill(21778, 1, 3000, NpcSkillTargetAttribute.ME);
		}
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (getOwner().getMoveController().getCurrentStep().isLastStep() && isDestinationReached() && isWalkingCompleted.compareAndSet(false, true))
			onDestinationArrived();
	}

	private void onDestinationArrived() {
		getSpawnTemplate().setWalkerId(null);
		WalkManager.stopWalking(this);
		spawnFlag();
		tryActivating();
	}

	private void tryActivating() {
		Npc otherAgent = getOtherAgent();
		if (otherAgent != null && PositionUtil.getDistance(getOwner(), otherAgent, false) <= 5) {
			getOwner().getEffectController().removeEffect(21779);
			getOwner().getLifeStats().setCurrentHpPercent(100);
			aggroOtherAgent(otherAgent);
			onReactiveThinking(otherAgent);
		} else {
			activationTask = ThreadPoolManager.getInstance().schedule(this::tryActivating, 5000);
		}
	}

	private Npc getOtherAgent() {
		return switch (getOwner().getNpcId()) {
			case 235064 -> getOwner().getPosition().getWorldMapInstance().getNpc(235065); // Veille
			case 235065 -> getOwner().getPosition().getWorldMapInstance().getNpc(235064); // Mastarius
			default -> null;
		};
	}

	private void aggroOtherAgent(Npc otherAgent) {
		getOwner().getAggroList().addHate(otherAgent, 100_000_000);
	}

	private void onReactiveThinking(Npc otherAgent) {
		canThink = true;
		getOwner().setTarget(otherAgent);
		getOwner().getGameStats().renewLastAttackTime();
		getOwner().getGameStats().renewLastAttackedTime();
		getOwner().getGameStats().renewLastChangeTargetTime();
		getOwner().getGameStats().renewLastSkillTime();
		setStateIfNot(AIState.FIGHT);
		think();
		getOwner().setState(CreatureState.ACTIVE, true);
		EmoteManager.emoteStartAttacking(getOwner(), otherAgent);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getOwner().getObjectId()));
		aggroResetTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(this::resetPlayerHate, 10000, 15000);
	}

	private void resetPlayerHate() {
		getAggroList().getList().stream().filter(info -> info.getAttacker() instanceof Player).forEach(info -> info.setHate(0));
	}

	private void spawnFlag() {
		if (flagNpc != null) {
			LoggerFactory.getLogger(EmpoweredAgent.class).warn("Tried to spawn flag for empowered agent {} twice!", getNpcId(), new Exception());
			return;
		}
		int flagNpcId = switch (getNpcId()) {
			case 235064 -> 832830; // Veille
			case 235065 -> 832831; // Mastarius
			default -> 0;
		};
		if (flagNpcId != 0) {
			WorldPosition pos = getPosition();
			flagNpc = (Npc) spawn(flagNpcId, pos.getX(), pos.getY(), pos.getZ(), pos.getHeading());
		}
	}

	private void despawnFlag() {
		if (flagNpc != null) {
			flagNpc.getController().delete();
			flagNpc = null;
		}
	}

	private void onGuardSpawnEvent() {
		int worldId = getOwner().getWorldId();
		float guardAmount = getOwner().getAggroList().getList().size() / 2.5f;
		if (guardAmount < 6)
			guardAmount = 6;
		for (int i = 0; i < guardAmount; i++) {
			Point3D pos = getRndPos();
			// TODO: change to dynamic siegeID
			SiegeSpawnTemplate template = SpawnEngine.newSiegeSpawn(worldId, Rnd.get(guardIds), 8011, SiegeRace.BALAUR, SiegeModType.SIEGE, pos.getX(),
				pos.getY(), pos.getZ(), (byte) 0);
			SpawnEngine.spawnObject(template, 1);
		}
	}

	private Point3D getRndPos() {
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		float distance = Rnd.nextFloat(10f);
		float x1 = (float) (Math.cos(angleRadians) * distance);
		float y1 = (float) (Math.sin(angleRadians) * distance);
		WorldPosition p = getPosition();
		return new Point3D(p.getX() + x1, p.getY() + y1, p.getZ());
	}

	private void cancelActivationTask() {
		if (activationTask != null && !activationTask.isDone())
			activationTask.cancel(true);
		if (aggroResetTask != null && !aggroResetTask.isDone())
			aggroResetTask.cancel(true);
	}

	@Override
	protected void handleDied() {
		despawnFlag();
		cancelActivationTask();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		despawnFlag();
		cancelActivationTask();
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN, REMOVE_EFFECTS_ON_MAP_REGION_DEACTIVATE -> false;
			default -> super.ask(question);
		};
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(SiegeConfig.FORTRESS_PROTECTOR_HEALTH_MULTIPLIER);
	}
}
