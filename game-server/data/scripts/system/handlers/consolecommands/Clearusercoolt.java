package consolecommands;

import java.util.ArrayList;
import java.util.List;
import java.util.Map.Entry;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PortalCooldown;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author ginho1
 */
public class Clearusercoolt extends ConsoleCommand {

	public Clearusercoolt() {
		super("clearusercoolt");
	}

	@Override
	public void execute(Player admin, String... params) {

		if (params.length < 1) {
			info(admin, null);
			return;
		}

		Player player = World.getInstance().getPlayer(Util.convertName(params[0]));

		if (player == null) {
			PacketSendUtility.sendMessage(admin, "Could not find an online player with that name.");
			return;
		}

		if (player.getPortalCooldownList() == null || player.getPortalCooldownList().getPortalCoolDowns() == null)
			return;

		List<Integer> mapIds = new ArrayList<>();
		for (Entry<Integer, PortalCooldown> mapId : player.getPortalCooldownList().getPortalCoolDowns().entrySet())
			mapIds.add(mapId.getKey());

		for (Integer id : mapIds)
			player.getPortalCooldownList().addPortalCooldown(id, 0);

		mapIds.clear();
		if (player.equals(admin))
			PacketSendUtility.sendMessage(admin, "Your instance cooldowns were removed.");
		else {
			PacketSendUtility.sendMessage(admin, "You have removed instance cooldowns of player: " + player.getName());
			PacketSendUtility.sendMessage(player, "Your instance cooldowns were removed by admin.");
		}
	}
}
