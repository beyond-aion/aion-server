package playercommands;

import java.util.Collection;

import com.aionemu.gameserver.model.gameobjects.player.FriendList;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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
		Collection<Player> availableStaffMembers = GMService.getInstance().getAvailableStaffMembers();
		if (availableStaffMembers.isEmpty()) {
			sendInfo(player, "There is no GM online.");
			return;
		}
		StringBuilder sb = new StringBuilder("GMs online (" + availableStaffMembers.size() + "):");
		for (Player gm : availableStaffMembers) {
			FriendList.Status status = gm.getFriendList().getStatus();
			sb.append("\n\t").append(name(gm)).append(" (").append(status.name().toLowerCase()).append(")");
		}
		sendInfo(player, sb.toString());
	}
}
