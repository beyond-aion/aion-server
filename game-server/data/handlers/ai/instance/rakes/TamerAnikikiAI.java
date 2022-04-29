package ai.instance.rakes;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI;

/**
 * @author xTz
 */
@AIName("tamer_anikiki")
public class TamerAnikikiAI extends GeneralNpcAI {

	private AtomicBoolean isStartedWalkEvent = new AtomicBoolean(false);

	public TamerAnikikiAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return false;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		super.handleCreatureMoved(creature);
		if (getNpcId() == 219040 && isInRange(creature, 10) && creature instanceof Player) {
			if (isStartedWalkEvent.compareAndSet(false, true)) {
				getSpawnTemplate().setWalkerId("3004600001");
				WalkManager.startWalking(this);
				getOwner().setState(CreatureState.ACTIVE, true);
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
				// Key Box
				spawn(700553, 611, 481, 936, (byte) 90);
				spawn(700553, 657, 482, 936, (byte) 60);
				spawn(700553, 626, 540, 936, (byte) 1);
				spawn(700553, 645, 534, 936, (byte) 75);
				PacketSendUtility.sendPacket((Player) creature, new SM_QUEST_ACTION(0, 180));
				PacketSendUtility.broadcastToMap(getOwner(), 1400262);
			}
		}
	}

	@Override
	protected void handleMoveArrived() {
		if (getNpcId() == 219040) {
			int point = getOwner().getMoveController().getCurrentStep().getStepIndex();
			if (point == 12) {
				getSpawnTemplate().setWalkerId(null);
				WalkManager.stopWalking(this);
				AIActions.deleteOwner(this);
				return;
			} else if (point == 8) {
				super.handleMoveArrived();
				getOwner().setState(CreatureState.WALK_MODE, true);
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
				return;
			}
		}
		super.handleMoveArrived();
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		if (getNpcId() != 219040) {
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					SkillEngine.getInstance().getSkill(getOwner(), 18189, 20, getOwner()).useNoAnimationSkill();
				}

			}, 5000);
		}
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		if (getNpcId() == 219037)
			return damage;
		else
			return 1;
	}
}
