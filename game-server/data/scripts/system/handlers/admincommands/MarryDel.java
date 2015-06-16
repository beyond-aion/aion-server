package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.WeddingService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author SheppeR
 */
public class MarryDel extends AdminCommand {

	public MarryDel() {
		super("marrydel");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length != 2) {
			PacketSendUtility.sendMessage(admin, "syntax //marrydel <characterName> <characterName>");
			return;
		}

		Player partner1 = World.getInstance().findPlayer(Util.convertName(params[0]));
		Player partner2 = World.getInstance().findPlayer(Util.convertName(params[1]));
		
		if (partner1 == null || partner2 == null) {
			PacketSendUtility.sendMessage(admin, "The specified player is not online.");
			return;
		}
		if (partner1.equals(partner2)) {
			PacketSendUtility.sendMessage(admin, "You can't cancel marry player on himself.");
			return;
		}
		if (partner1.getWorldId() == 510010000 || partner1.getWorldId() == 520010000 || partner2.getWorldId() == 510010000 || partner2.getWorldId() == 520010000) {
			PacketSendUtility.sendMessage(admin, "One of the players is in prison.");
			return;
		}

		WeddingService.getInstance().unDoWedding(partner1, partner2);
		PacketSendUtility.sendMessage(admin, "Married canceled.");
	}

	@Override
	public void onFail(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax //marrydel <characterName> <characterName>");
	}
}
