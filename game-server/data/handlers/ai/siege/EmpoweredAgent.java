package ai.siege;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.geometry.Point3D;
import com.aionemu.gameserver.model.siege.SiegeModType;
import com.aionemu.gameserver.model.siege.SiegeRace;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.model.templates.spawns.siegespawns.SiegeSpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("empowered_agent")
public class EmpoweredAgent extends AggressiveNpcAI {

	public EmpoweredAgent(Npc owner) {
		super(owner);
	}

	private final List<Integer> guardIds = new ArrayList<>();
	private final List<Integer> percents = new ArrayList<>();
	private boolean canThink = true;
	private Npc flagNpc = null;

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
			case 235064 -> Collections.addAll(guardIds, 235334, 235335, 235336, 235337, 235338, 235339);
			case 235065 -> Collections.addAll(guardIds, 235340, 235341, 235342, 235343, 235344, 235345);
		}
		Collections.addAll(percents, 80, 70, 60, 50, 40, 30, 25, 20, 5);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				switch (percent) {
					case 80, 70, 60, 50, 40, 30, 20 -> onGuardSpawnEvent();
					case 25, 5 -> getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21778, 1, 100, 0, 3000, NpcSkillTargetAttribute.ME)));
				}
				percents.remove(percent);
				break;
			}
		}
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		String walkerId = getOwner().getSpawn().getWalkerId();
		if (walkerId == null)
			return;
		int step = getOwner().getMoveController().getCurrentStep().getStepIndex();
		int stop = switch (getOwner().getNpcId()) {
			case 235064 -> 48;
			case 235065 -> 37;
			default -> 0;
		};
		if (stop == step)
			ThreadPoolManager.getInstance().schedule(this::onDestinationArrived, 5000);
	}

	private void onDestinationArrived() {
		getSpawnTemplate().setWalkerId(null);
		getSpawnTemplate().setX(getOwner().getX());
		getSpawnTemplate().setY(getOwner().getY());
		getSpawnTemplate().setZ(getOwner().getZ());
		WalkManager.stopWalking(this);
		getOwner().getEffectController().removeEffect(21779);
		getOwner().getLifeStats().setCurrentHpPercent(100);
		spawnFlag();
		onAddHateEvent();
		onReactiveThinking();
	}

	private void onAddHateEvent() {
		Npc target = switch (getOwner().getNpcId()) {
			case 235064 -> getOwner().getPosition().getWorldMapInstance().getNpc(235065); // Veille
			case 235065 -> getOwner().getPosition().getWorldMapInstance().getNpc(235064); // Mastarius
			default -> null;
		};
		if (target != null)
			getOwner().getAggroList().addHate(target, Integer.MAX_VALUE / 2);
	}

	private void onReactiveThinking() {
		canThink = true;
		Creature creature = getAggroList().getMostHated();
		if (creature == null || creature.isDead() || !getOwner().canSee(creature)) {
			setStateIfNot(AIState.FIGHT);
			think();
		} else {
			getMoveController().abortMove();
			getOwner().setTarget(creature);
			getOwner().getGameStats().renewLastAttackTime();
			getOwner().getGameStats().renewLastAttackedTime();
			getOwner().getGameStats().renewLastChangeTargetTime();
			getOwner().getGameStats().renewLastSkillTime();
			setStateIfNot(AIState.FIGHT);
			getOwner().setState(CreatureState.ACTIVE, true);
			handleMoveValidate();
			EmoteManager.emoteStartAttacking(getOwner(), creature);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getOwner().getObjectId()));
		}
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
		float direction = Rnd.get(0, 199) / 100f;
		float distance = Rnd.get() * 10;
		float x1 = (float) (Math.cos(Math.PI * direction) * distance);
		float y1 = (float) (Math.sin(Math.PI * direction) * distance);
		WorldPosition p = getPosition();
		return new Point3D(p.getX() + x1, p.getY() + y1, p.getZ());
	}

	@Override
	protected void handleDied() {
		despawnFlag();
		super.handleDied();
	}

	@Override
	protected void handleDespawned() {
		despawnFlag();
		super.handleDespawned();
	}

	@Override
	public boolean ask(AIQuestion question) {
		if (question == AIQuestion.SHOULD_RESPAWN)
			return false;
		return super.ask(question);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(SiegeConfig.SIEGE_HEALTH_MULTIPLIER);
	}
}
