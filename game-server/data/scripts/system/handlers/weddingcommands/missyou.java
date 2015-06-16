package weddingcommands;

import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.chathandlers.WeddingCommand;

/**
 * @author synchro2
 */
public class missyou extends WeddingCommand {
	
	public missyou() {
		super("missyou");
	}

	@Override
	public void execute(final Player player, String... params) {

		Player partner = player.findPartner();

		if (partner == null) {
			PacketSendUtility.sendMessage(player, "Not online.");
			return;
		}
		if (player.getWorldId() == 510010000 || player.getWorldId() == 520010000) {
			PacketSendUtility.sendMessage(player, "You can't use this command on prison.");
			return;
		}

		if (partner.getWorldId() == 510010000 || partner.getWorldId() == 520010000) {
			PacketSendUtility.sendMessage(player, "You can't teleported to " + partner.getName() +", your partner is on prison.");
			return;
		}
		
		if(partner.isInInstance()) {
			PacketSendUtility.sendMessage(player, "You can't teleported to " + partner.getName() +", your partner is in Instance.");
			return;
		}

		if(!player.isCommandInUse()) {
			TeleportService2.teleportTo(player, partner.getWorldId(), partner.getInstanceId(), partner.getX(), partner.getY(),
			partner.getZ(), partner.getHeading(), TeleportAnimation.BEAM_ANIMATION);
			PacketSendUtility.sendMessage(player, "Teleported to player " + partner.getName() + ".");
			player.setCommandUsed(true);
			
			ThreadPoolManager.getInstance().schedule(new Runnable() {
				@Override
				public void run() {
					player.setCommandUsed(false);
				}
			}, 60 * 60 * 1000);
		}
		else
			PacketSendUtility.sendMessage(player, "Only 1 TP per hour.");
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Failed");
	}
}
