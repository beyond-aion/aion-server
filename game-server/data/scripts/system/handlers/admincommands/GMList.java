package admincommands;

import java.util.Collection;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.gameobjects.player.FriendList.Status;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.GMService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Aion Gates
 * @modified Neon
 */
public class GMList extends AdminCommand {

	public GMList() {
		super("gmlist");
	}

	@Override
	public void execute(Player admin, String... params) {

		String sGMNames = "";
		Collection<Player> gms = GMService.getInstance().getAnnounceGMs();
		int GMCount = 0;

		for (Player pPlayer : gms) {
			if (pPlayer.isGM() && !pPlayer.isProtectionActive() && pPlayer.getFriendList().getStatus() != FriendList.Status.OFFLINE) {
				GMCount++;

				sGMNames += " " + pPlayer.getName(AdminConfig.CUSTOMTAG_ENABLE) + " (" + returnStringStatus(pPlayer.getFriendList().getStatus()) + ")\n";
			}
		}

		if (GMCount == 0) {
			PacketSendUtility.sendMessage(admin, "There is no GM online.");
		}
		else if (GMCount == 1) {
			PacketSendUtility.sendMessage(admin, "There is " + GMCount + " GM online.");
		}
		else {
			PacketSendUtility.sendMessage(admin, "There are " + GMCount + " GMs online.");
		}
		if (GMCount != 0)
			PacketSendUtility.sendMessage(admin, "List: \n" + sGMNames);
	}

	private String returnStringStatus(Status p_status) {
		String return_string = "";
		if (p_status == FriendList.Status.ONLINE)
			return_string = "online";
		if (p_status == FriendList.Status.AWAY)
			return_string = "away";
		return return_string;
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
