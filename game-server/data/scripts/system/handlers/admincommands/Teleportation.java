package admincommands;

import java.awt.Color;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author cura
 * @modified Neon
 */
public class Teleportation extends AdminCommand {

	public Teleportation() {
		super("teleportation", "Toggles teleportation mode when moving via mouse.");
	}

	@Override
	public void execute(Player admin, String... params) {
		admin.setAdminTeleportation(!admin.isAdminTeleportation());
		sendInfo(admin, "Teleportation mode is now " + (admin.isAdminTeleportation() ?ChatUtil.color("active", Color.GREEN) : ChatUtil.color("inactive", Color.RED)) + ".");
	}
}
