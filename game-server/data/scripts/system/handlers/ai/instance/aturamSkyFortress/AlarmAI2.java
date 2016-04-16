package ai.instance.aturamSkyFortress;

import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI2;

/**
 * @author xTz
 */
@AIName("alarm")
public class AlarmAI2 extends AggressiveNpcAI2 {

	private boolean canThink = true;
	private AtomicBoolean startedEvent = new AtomicBoolean(false);

	@Override
	public boolean canThink() {
		return canThink;
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature instanceof Player) {
			final Player player = (Player) creature;
			if (MathUtil.getDistance(getOwner(), player) <= 23) {
				if (startedEvent.compareAndSet(false, true)) {
					canThink = false;
					PacketSendUtility.broadcastMessage(getOwner(), 1500380);
					PacketSendUtility.broadcastPacket(getOwner(), SM_SYSTEM_MESSAGE.STR_MSG_Station_DoorCtrl_Evileye());
					getSpawnTemplate().setWalkerId("3002400002");
					WalkManager.startWalking(this);
					getOwner().setState(1);
					PacketSendUtility.broadcastPacket(getOwner(), new SM_EMOTION(getOwner(), EmotionType.START_EMOTE2, 0, getObjectId()));
					getPosition().getWorldMapInstance().getDoors().get(128).setOpen(true);
					getPosition().getWorldMapInstance().getDoors().get(138).setOpen(true);
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {
							if (!isAlreadyDead()) {
								despawn();
							}
						}

					}, 3000);
				}
			}
		}
	}

	private void despawn() {
		AI2Actions.deleteOwner(this);
	}
}
