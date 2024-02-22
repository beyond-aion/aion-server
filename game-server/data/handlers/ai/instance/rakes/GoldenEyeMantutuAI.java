package ai.instance.rakes;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("golden_eye_mantutu")
public class GoldenEyeMantutuAI extends AggressiveNpcAI {

	private boolean canThink = true;
	private final AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> hungerTask;

	public GoldenEyeMantutuAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleCustomEvent(int eventId, Object... args) {
		if (eventId == 1 && args != null) {
			canThink = false;
			getMoveController().abortMove();
			EmoteManager.emoteStopAttacking(getOwner());
			Npc npc = (Npc) args[0];
			getOwner().setTarget(npc);
			setStateIfNot(AIState.FOLLOWING);
			getMoveController().moveToTargetObject();
			getOwner().setState(CreatureState.ACTIVE, true);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
		}
	}

	@Override
	protected void handleMoveArrived() {
		super.handleMoveArrived();
		if (!canThink) {
			VisibleObject target = getTarget();
			getMoveController().abortMove();
			if (target != null && target.isSpawned() && target instanceof Npc npc) {
				if (npc.getNpcId() == 281128 || npc.getNpcId() == 281129) {
					startFeedTime(npc);
				}
			}
		}
	}

	private void startFeedTime(final Npc npc) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!isDead() && npc != null) {
				switch (npc.getNpcId()) {
					case 281128: // Feed Supply Device
						getEffectController().removeEffect(20489);
						spawn(701386, 716.508f, 508.571f, 939.607f, (byte) 119);
						break;
					case 281129: // Water Supply Device
						getEffectController().removeEffect(20490);
						spawn(701387, 716.389f, 494.207f, 939.607f, (byte) 119);
						break;
				}
				npc.getController().delete();
				canThink = true;
				Creature creature = getAggroList().getMostHated();
				if (creature == null || creature.isDead() || !getOwner().canSee(creature)) {
					setStateIfNot(AIState.FIGHT);
					think();
				} else {
					getOwner().setTarget(creature);
					getOwner().getGameStats().renewLastAttackTime();
					getOwner().getGameStats().renewLastAttackedTime();
					getOwner().getGameStats().renewLastChangeTargetTime();
					getOwner().getGameStats().renewLastSkillTime();
					setStateIfNot(AIState.FIGHT);
					handleMoveValidate();
				}
			}
		}, 6000);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case IS_IMMUNE_TO_ABNORMAL_STATES -> true;
			default -> super.ask(question);
		};
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			doSchedule();
		}
	}

	@Override
	protected void handleDespawned() {
		cancelHungerTask();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelHungerTask();
		Npc npc = getPosition().getWorldMapInstance().getNpc(219037);
		if (npc != null && !npc.isDead()) {
			npc.getEffectController().removeEffect(18189);
		}
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		cancelHungerTask();
		getEffectController().removeEffect(20489);
		getEffectController().removeEffect(20490);
		canThink = true;
		isHome.set(true);
		super.handleBackHome();
	}

	private void doSchedule() {
		hungerTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			int skill = Rnd.nextBoolean() ? 20489 : 20490; // Hunger / Thirst
			SkillEngine.getInstance().getSkill(getOwner(), skill, 20, getOwner()).useNoAnimationSkill();
		}, 10000, 30000);
	}

	private void cancelHungerTask() {
		if (hungerTask != null && !hungerTask.isDone()) {
			hungerTask.cancel(true);
		}
	}

}
