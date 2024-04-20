package playercommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.audit.GMService;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Aion Gates, Neon
 */
public class GmList extends PlayerCommand {

	public GmList() {
		super("gmlist", "Lists all available team members.");
	}

	@Override
	public void execute(Player player, String... params) {
		StringBuilder sb = new StringBuilder();
		int count = 0;

		for (Player gm : GMService.getInstance().getOnlineStaffMembers()) {
			FriendList.Status status = gm.getFriendList().getStatus();
			if (!gm.isInCustomState(CustomPlayerState.NO_WHISPERS_MODE) && status != FriendList.Status.OFFLINE) {
				sb.append("\n\t" + ChatUtil.name(gm) + " (" + status.name().toLowerCase() + ")");
				count++;
			}
		}

		if (count == 0) {
			sendInfo(player, "There is no GM online.");
			return;
		}
		sendInfo(player, "GMs online (" + count + "):" + sb.toString());
	}
}
