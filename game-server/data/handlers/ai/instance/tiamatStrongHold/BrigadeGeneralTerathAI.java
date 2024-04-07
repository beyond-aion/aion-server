package ai.instance.tiamatStrongHold;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;

import ai.AggressiveNpcAI;

/**
 * @author Cheatkiller
 */
@AIName("brigadegeneralterath")
public class BrigadeGeneralTerathAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(90, 70, 50, 30, 25);
	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> skillTask;
	private boolean canThink = true;
	private Npc aethericField;
	private boolean isGravityEvent;
	private boolean isFinalBuff;

	public BrigadeGeneralTerathAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			if (aethericField == null) {
				aethericField = (Npc) spawn(730692, 1030.08f, 1030.08f, 1030.08f, (byte) 0);
				getPosition().getWorldMapInstance().setDoorState(706, false);
			}
			if (!isGravityEvent) {
				startSkillTask();
			}
		}
		hpPhases.tryEnterNextPhase(this);
		if (!isFinalBuff && getOwner().getLifeStats().getHpPercentage() <= 25) {
			isFinalBuff = true;
			AIActions.useSkill(this, 20942);
		}
	}

	private void startSkillTask() {
		skillTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead())
				gravityDistortionEvent();
		}, 5000, 30000);
	}

	private void cancelskillTask() {
		if (skillTask != null && !skillTask.isCancelled()) {
			skillTask.cancel(true);
		}
	}

	private void gravityDistortionEvent() {
		SkillEngine.getInstance().getSkill(getOwner(), 20739, 55, getOwner()).useNoAnimationSkill();
		spawn(283096, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);// 4.0
		spawn(283097, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);// 4.0
		spawn(283098, getOwner().getX(), getOwner().getY(), getOwner().getZ(), (byte) 0);// 4.0
		ThreadPoolManager.getInstance().schedule(() -> SkillEngine.getInstance().getSkill(getOwner(), 20741, 55, getOwner()).useNoAnimationSkill(), 5000);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		if (isGravityEvent)
			return;
		canThink = false;
		isGravityEvent = true;
		cancelskillTask();
		spawn(283558, 1056.8f, 297.6f, 409.9f, (byte) 0); // TODO find Right ID 4.0
		spawn(283558, 1002.07f, 297.4f, 409.85f, (byte) 0); // TODO find Right ID 4.0
		SkillEngine.getInstance().getSkill(getOwner(), 20737, 55, getOwner()).useNoAnimationSkill();
		ThreadPoolManager.getInstance().schedule(() -> {
			EmoteManager.emoteStopAttacking(getOwner());
			setStateIfNot(AIState.WALKING);
			getOwner().getMoveController().moveToPoint(getOwner().getSpawn().getX(), getOwner().getSpawn().getY(), getOwner().getSpawn().getZ());
			WalkManager.startWalking(BrigadeGeneralTerathAI.this);
			getOwner().setState(CreatureState.ACTIVE, true);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getOwner().getObjectId()));
		}, 4000);
		ThreadPoolManager.getInstance().schedule(() -> {
			spawn(283109, 1029.9f, 297.26f, 409.08f, (byte) 0);// 4.0
			spawn(283110, 1029.93f, 297.31f, 409.08f, (byte) 0);// 4.0
		}, 10000);
		ThreadPoolManager.getInstance().schedule(() -> {
			if (isDead())
				return;
			despawn();
			getEffectController().removeEffect(20737);
			canThink = true;
			isGravityEvent = false;
			startSkillTask();
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
				setStateIfNot(AIState.WALKING);
				getOwner().setState(CreatureState.ACTIVE, true);
				getOwner().getMoveController().moveToTargetObject();
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getOwner().getObjectId()));
			}
		}, 30000);
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null) {
				npc.getController().delete();
			}
		}
	}

	@Override
	protected void handleDied() {
		super.handleDied();
		cancelskillTask();
		aethericField.getController().delete();
		getPosition().getWorldMapInstance().setDoorState(706, true);
		despawn();
	}

	private void despawn() {
		WorldMapInstance instance = getPosition().getWorldMapInstance();
		deleteNpcs(instance.getNpcs(283558)); // TODO find Right ID 4.0
		deleteNpcs(instance.getNpcs(283109)); // 4.0
		deleteNpcs(instance.getNpcs(283110)); // 4.0
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
		isFinalBuff = false;
		cancelskillTask();
		isGravityEvent = false;
		canThink = true;
		isHome.set(true);
		aethericField.getController().delete();
		despawn();
		getPosition().getWorldMapInstance().setDoorState(706, true);
	}

	@Override
	protected void handleDespawned() {
		super.handleDespawned();
		cancelskillTask();
	}

	@Override
	public boolean canThink() {
		return canThink;
	}
}
