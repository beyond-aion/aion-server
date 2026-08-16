package consolecommands;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author ginho1
 */
public class Clearusercoolt extends ConsoleCommand {

	public Clearusercoolt() {
		super("clearusercoolt", "Clears cooldowns for instances.");

		setSyntaxInfo("[player] - Removes the instance cooldowns of the given player (defaults to your target, or your character, if nothing is targeted).");
	}

	@Override
	public void execute(Player admin, String... params) {
		Player player;
		if (params.length == 0) {
			player = admin.getTarget() instanceof Player target ? target : admin;
		} else {
			String playerName = ChatUtil.getRealCharName(params[0], true);
			player = World.getInstance().getPlayer(playerName);
			if (player == null) {
				PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
				return;
			}
		}
		clearAllInstanceCooldowns(admin, player);
	}

	public static void clearAllInstanceCooldowns(Player admin, Player player) {
		if (player.getPortalCooldownList().getPortalCoolDowns() == null) {
			PacketSendUtility.sendMessage(admin, (player.equals(admin) ? "You have" : player.getName() + " has") + " no instance cooldowns to remove.");
			return;
		}

		List<Integer> worldIds = player.getPortalCooldownList().getPortalCoolDowns().keySet().stream().toList();
		player.getPortalCooldownList().setPortalCoolDowns(null);
		worldIds.forEach(player.getPortalCooldownList()::sendEntryInfo);

		if (player.equals(admin)) {
			PacketSendUtility.sendMessage(admin, "Your instance cooldowns were removed.");
		} else {
			PacketSendUtility.sendMessage(admin, "You have removed instance cooldowns of " + player.getName() + '.');
			PacketSendUtility.sendMessage(player, admin.getName(true) + " removed your instance cooldowns.");
		}
	}
}
