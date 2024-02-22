package ai.instance.abyssal_splinter;

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
import com.aionemu.gameserver.model.templates.ai.Percentage;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.SummonerAI;

/**
 * @author Luzien
 */
@AIName("kaluva")
public class KaluvaAI extends SummonerAI {

	private boolean canThink = true;

	public KaluvaAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleIndividualSpawnedSummons(Percentage percent) {
		spawn();
		canThink = false;
		EmoteManager.emoteStopAttacking(getOwner());
		setStateIfNot(AIState.FOLLOWING);
		getOwner().setState(CreatureState.ACTIVE, true);
		PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
		AIActions.targetCreature(this, getPosition().getWorldMapInstance().getNpc(281902));
		getMoveController().moveToTargetObject();
	}

	@Override
	protected void handleMoveArrived() {
		if (!canThink) {
			Npc egg = getPosition().getWorldMapInstance().getNpc(281902);
			if (egg != null) {
				SkillEngine.getInstance().getSkill(getOwner(), 19223, 55, egg).useNoAnimationSkill();
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
		}
		super.handleMoveArrived();
	}

	private void spawn() {
		switch (Rnd.get(1, 4)) {
			case 1 -> spawn(281902, 663.322021f, 556.731995f, 424.295013f, (byte) 64);
			case 2 -> spawn(281902, 644.0224f, 523.9641f, 423.09103f, (byte) 32);
			case 3 -> spawn(281902, 611.008f, 539.73395f, 423.25034f, (byte) 119);
			case 4 -> spawn(281902, 628.4426f, 585.4443f, 424.31854f, (byte) 93);
		}
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
