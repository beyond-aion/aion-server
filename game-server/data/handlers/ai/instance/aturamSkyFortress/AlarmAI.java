package ai.instance.aturamSkyFortress;

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
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("alarm")
public class AlarmAI extends AggressiveNpcAI {

	private boolean canThink = true;
	private AtomicBoolean startedEvent = new AtomicBoolean(false);

	public AlarmAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player player) {
			if (PositionUtil.getDistance(getOwner(), player) <= 23) {
				if (startedEvent.compareAndSet(false, true)) {
					canThink = false;
					PacketSendUtility.broadcastMessage(getOwner(), 1500380);
					PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_Station_DoorCtrl_Evileye());
					getSpawnTemplate().setWalkerId("3002400002");
					WalkManager.startWalking(this);
					getOwner().setState(CreatureState.ACTIVE, true);
					PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.CHANGE_SPEED, 0, getObjectId()));
					// both doors are inverted, they visually close by opening.
					// they also have no collision but there are invisible walls anyway, as you are never supposed to enter the rooms behind them
					getPosition().getWorldMapInstance().setDoorState(128, true);
					getPosition().getWorldMapInstance().setDoorState(138, true);
					ThreadPoolManager.getInstance().schedule(() -> {
						if (!isDead()) {
							despawn();
						}
					}, 3000);
				}
			}
		}
	}

	private void despawn() {
		AIActions.deleteOwner(this);
	}
}
