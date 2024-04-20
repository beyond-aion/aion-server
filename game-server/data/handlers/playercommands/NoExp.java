package playercommands;

import java.awt.Color;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.PlayerCommand;

/**
 * @author Wakizashi, Neon
 */
public class NoExp extends PlayerCommand {

	public NoExp() {
		super("noexp", "Enables/disables your ability to gain experience.");
	}

	@Override
	public void execute(Player player, String... params) {
		PlayerCommonData pcd = player.getCommonData();

		pcd.setNoExp(!pcd.getNoExp());
		sendInfo(player, "Experience rewards are now " + (pcd.getNoExp() ? ChatUtil.color("inactive", Color.RED) : ChatUtil.color("active", Color.GREEN)) + ".");
	}
}
