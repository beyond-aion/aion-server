package admincommands;

import java.awt.Color;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author cura, Neon
 */
public class Teleportation extends AdminCommand {

	public Teleportation() {
		super("teleportation", "Toggles teleportation mode when moving via mouse.");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (admin.isInCustomState(CustomPlayerState.TELEPORTATION_MODE))
			admin.unsetCustomState(CustomPlayerState.TELEPORTATION_MODE);
		else
			admin.setCustomState(CustomPlayerState.TELEPORTATION_MODE);
		sendInfo(admin, "Teleportation mode is now " + (admin.isInCustomState(CustomPlayerState.TELEPORTATION_MODE)
			? ChatUtil.color("active", Color.GREEN) : ChatUtil.color("inactive", Color.RED)) + ".");
	}
}
