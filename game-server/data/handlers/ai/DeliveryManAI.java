package ai;

import java.time.Duration;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.clientpackets.CM_READ_EXPRESS_MAIL;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author -Nemesiss-, Neon
 */
@AIName("deliveryman")
public class DeliveryManAI extends FollowingNpcAI {

	private static final int WAIT_TIME = (int) Duration.ofMinutes(2).toMillis(); // leaves if the owner doesn't talk to him in time
	private static final int SERVICE_TIME = (int) Duration.ofMinutes(10).toMillis(); // leaves this long after the owner last talked to him
	private int despawnMessageId = 390267; // Whiririkk, let's go!

	public DeliveryManAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		scheduleLeave(WAIT_TIME);
	}

	private void scheduleLeave(int delayMillis) {
		getOwner().getController().cancelTask(TaskId.DESPAWN);
		getOwner().getController().addTask(TaskId.DESPAWN, ThreadPoolManager.getInstance().schedule(() -> {
			despawnMessageId = 390268; // Time is silver my friend, akakak!
			AIActions.deleteOwner(DeliveryManAI.this);
		}, delayMillis));
	}

	@Override
	protected void handleDespawned() {
		PacketSendUtility.broadcastMessage(getOwner(), despawnMessageId);
		Player player = World.getInstance().getPlayer(getCreatorId());
		if (player != null) {
			if (getOwner().equals(player.getPostman())) {
				player.setPostman(null);
				CM_READ_EXPRESS_MAIL.startPostmanCooldown(player);
			}
			player.unsetStorageManager(getOwner());
		}
		super.handleDespawned();
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		if (getCreatorId() == creature.getObjectId()) {
			handleFollowMe(creature);
			handleCreatureMoved(creature);
			PacketSendUtility.broadcastMessage(getOwner(), 390266, 500); // Here is your mail, akakak!
		}
	}

	@Override
	protected void handleCreatureNotSee(Creature creature) {
		if (getCreatorId() == creature.getObjectId())
			getOwner().getController().delete();
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (getCreatorId() == player.getObjectId()) {
			scheduleLeave(SERVICE_TIME);
			player.getMailbox().open(true, getOwner());
			PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), DialogPage.MAIL.id()));
		} else {
			PacketSendUtility.broadcastMessage(getOwner(), 390269); // There is no mail for you, nyerk.
		}
	}
}
