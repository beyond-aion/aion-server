package ai;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.handler.FollowEventHandler;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.npcshout.NpcShout;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author -Nemesiss-
 */
@AIName("deliveryman")
public class DeliveryManAI2 extends FollowingNpcAI2 {

	public static int EVENT_SET_CREATOR = 1;
	private static int SERVICE_TIME = 5 * 60 * 1000;
	private static int SPAWN_ACTION_DELAY = 1500;
	private Player owner;
	private Future<?> despawnTask;

	@Override
	protected void handleSpawned() {
		despawnTask = ThreadPoolManager.getInstance().schedule(new DeleteDeliveryMan(), SERVICE_TIME);
		ThreadPoolManager.getInstance().schedule(new DeliveryManSpawnAction(), SPAWN_ACTION_DELAY);
		super.handleSpawned();
	}

	@Override
	protected void handleDespawned() {
		PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 390267, getObjectId(), 1, new NpcShout().getParam()));
		owner.setPostman(null);
		super.handleDespawned();
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (player.equals(owner)) {
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 18));
			// player.getMailbox().sendMailList(true);
		}
	}

	@Override
	protected void handleDialogFinish(Player player) {
		super.handleDialogFinish(player);
		if (player.getObjectId().equals(owner.getObjectId())) {
			AI2Actions.deleteOwner(DeliveryManAI2.this);
			if (despawnTask != null) {
				despawnTask.cancel(false);
			}
		}
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		if (creature == owner)
			FollowEventHandler.creatureMoved(this, creature);
	}

	@Override
	protected void handleCustomEvent(int eventId, Object... args) {
		if (eventId == EVENT_SET_CREATOR)
			owner = (Player) args[0];
	}

	private final class DeleteDeliveryMan implements Runnable {

		@Override
		public void run() {
			AI2Actions.deleteOwner(DeliveryManAI2.this);
		}

	}

	private final class DeliveryManSpawnAction implements Runnable {

		@Override
		public void run() {
			PacketSendUtility.broadcastPacket(getOwner(), new SM_SYSTEM_MESSAGE(true, 390266, getObjectId(), 1, new NpcShout().getParam()));
			handleFollowMe(owner);
			handleCreatureMoved(owner);
		}

	}

}
