package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.network.loginserver.LoginServer;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Neon
 */
public class Grant extends AdminCommand {

	public Grant() {
		super("grant", "Grants/revokes account permissions.");

		// @formatter:off
		setSyntaxInfo(
				"<a> <level> [name] - Grants the specified access level (default: target's account, optional: specified character's account). 0 will remove the account's access level.",
				"<m> <level> [name] - Grants the specified membership level (default: target's account, optional: specified character's account). 0 will remove the account's membership level."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 2) {
			sendInfo(admin);
			return;
		}

		int type;
		if ("a".equalsIgnoreCase(params[0])) {
			type = 1;
		} else if ("m".equalsIgnoreCase(params[0])) {
			type = 2;
		} else {
			sendInfo(admin);
			return;
		}

		int level = Integer.parseInt(params[1]);
		if (level < 0) {
			sendInfo(admin, "Level must not be negative.");
			return;
		}

		Player player;
		if (params.length >= 3) {
			String playerName = Util.convertName(params[2]);
			player = World.getInstance().getPlayer(playerName);
			if (player == null) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
				return;
			}
		} else if (admin.getTarget() instanceof Player target) {
			player = target;
		} else {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_INVALID_TARGET());
			return;
		}
		if (type == 1) {
			if (!player.equals(admin) && player.getAccount().getAccessLevel() >= admin.getAccount().getAccessLevel()) {
				sendInfo(admin, "You are not allowed change the access level of players with the same or higher access level than your own.");
				return;
			}
		}

		LoginServer.getInstance().sendLsControlPacket(type, level, player, admin);
	}
}
