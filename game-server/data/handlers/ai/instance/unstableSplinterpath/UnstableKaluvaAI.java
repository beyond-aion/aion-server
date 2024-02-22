package ai.instance.unstableSplinterpath;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, Cheatkiller
 */
@AIName("unstablekaluva")
public class UnstableKaluvaAI extends AggressiveNpcAI {

	private boolean canThink = true;
	private boolean isInMove = false;

	public UnstableKaluvaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (Rnd.chance() < 3) {
			if (!isInMove) {
				isInMove = true;
				moveToSpawner(randomEgg());
			}
		}
	}

	private void moveToSpawner(int egg) {
		Npc spawner = getPosition().getWorldMapInstance().getNpc(egg);
		if (spawner != null) {
			SkillEngine.getInstance().getSkill(getOwner(), 19152, 55, getOwner()).useNoAnimationSkill();
			canThink = false;
			EmoteManager.emoteStopAttacking(getOwner());
			setStateIfNot(AIState.FOLLOWING);
			getOwner().setState(CreatureState.ACTIVE, true);
			PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
			AIActions.targetCreature(this, getPosition().getWorldMapInstance().getNpc(egg));
			getMoveController().moveToTargetObject();
		}
	}

	@Override
	protected void handleMoveArrived() {
		if (!canThink) {
			if (getOwner().getTarget()instanceof Npc spawner) {
				spawner.getEffectController().removeEffect(19222);
				SkillEngine.getInstance().getSkill(getOwner(), 19223, 55, spawner).useNoAnimationSkill();
				getEffectController().removeEffect(19152);
			}

			ThreadPoolManager.getInstance().schedule(() -> {
				canThink = true;
				Creature creature = getAggroList().getMostHated();
				if (creature != null && getOwner().canSee(creature) && !creature.isDead()) {
					getOwner().setTarget(creature);
					getOwner().getGameStats().renewLastAttackTime();
					getOwner().getGameStats().renewLastAttackedTime();
					getOwner().getGameStats().renewLastChangeTargetTime();
					getOwner().getGameStats().renewLastSkillTime();
				}
				setStateIfNot(AIState.FIGHT);
				think();
			}, 2000);
			isInMove = false;
		}
		super.handleMoveArrived();
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		isInMove = false;
	}

	private int randomEgg() {
		int[] npcIds = { 219971, 219952, 219970, 219969 };
		return Rnd.get(npcIds);
	}

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case REWARD_LOOT, REWARD_AP -> false;
			default -> super.ask(question);
		};
	}

}
