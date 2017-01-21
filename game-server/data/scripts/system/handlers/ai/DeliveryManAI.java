package ai;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author -Nemesiss-, Neon
 */
@AIName("deliveryman")
public class DeliveryManAI extends FollowingNpcAI {

	private static int SERVICE_TIME = 5 * 60 * 1000;
	private Player owner;

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		owner = getOwner().getPosition().getWorldMapInstance().getPlayer(getOwner().getCreatorId());
		getOwner().getController().addTask(TaskId.DESPAWN,
			ThreadPoolManager.getInstance().schedule(() -> AIActions.deleteOwner(DeliveryManAI.this), SERVICE_TIME));
		handleFollowMe(owner);
		handleCreatureMoved(owner);
		PacketSendUtility.broadcastMessage(getOwner(), 390266, 1500); // Here is your mail, akakak!
		PacketSendUtility.broadcastMessage(getOwner(), 390268, 30000); // Time is silver my friend, akakak!
	}

	@Override
	protected void handleDespawned() {
		PacketSendUtility.broadcastMessage(getOwner(), 390267); // Whiririkk, let's go!
		owner.setPostman(null);
		super.handleDespawned();
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (player.equals(owner))
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.MAIL.id()));
		else
			PacketSendUtility.broadcastMessage(getOwner(), 390269); // There is no mail for you, nyerk.
	}
}
