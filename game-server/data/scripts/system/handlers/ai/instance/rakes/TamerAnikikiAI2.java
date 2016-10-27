package ai.instance.rakes;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUEST_ACTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.GeneralNpcAI2;

/**
 * @author xTz
 */
@AIName("tamer_anikiki")
public class TamerAnikikiAI2 extends GeneralNpcAI2 {

	private AtomicBoolean isStartedWalkEvent = new AtomicBoolean(false);

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
				getOwner().setState(1);
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
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
		int point = getOwner().getMoveController().getCurrentPoint();
		super.handleMoveArrived();
		if (getNpcId() == 219040) {
			if (point == 8) {
				getOwner().setState(64);
				PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
			}
			if (point == 12) {
				getSpawnTemplate().setWalkerId(null);
				WalkManager.stopWalking(this);
				AI2Actions.deleteOwner(this);
			}
		}
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
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		if (getNpcId() == 219037)
			return damage;
		else
			return 1;
	}
}
