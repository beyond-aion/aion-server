package admincommands;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Centisgood(Barahime)
 */
public class SetRace extends AdminCommand {

	public SetRace() {
		super("setrace");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length < 1) {
			PacketSendUtility.sendMessage(admin, "syntax: //setrace <elyos | asmodians>");
			return;
		}

		VisibleObject visibleobject = admin.getTarget();

		if (visibleobject == null || !(visibleobject instanceof Player)) {
			PacketSendUtility.sendMessage(admin, "Wrong select target.");
			return;
		}

		Player target = (Player) visibleobject;
		if (params[0].equalsIgnoreCase("elyos")) {
			target.getCommonData().setRace(Race.ELYOS);
			TeleportService2.teleportTo(target, WorldMapType.SANCTUM.getId(), 1322f, 1511f, 568f, (byte)0);
			PacketSendUtility.sendMessage(target, "Has been moved to Sanctum.");
		}
		else if (params[0].equalsIgnoreCase("asmodians")) {
			target.getCommonData().setRace(Race.ASMODIANS);
			TeleportService2.teleportTo(target, WorldMapType.PANDAEMONIUM.getId(), 1679f, 1400f, 195f, (byte)0);
			PacketSendUtility.sendMessage(target, "Has been moved to Pandaemonium");
		}
		PacketSendUtility.sendMessage(admin,
			target.getName() + " race has been changed to " + params[0] + ".\n" + target.getName()
				+ " has been moved to town.");
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax: //setrace <elyos | asmodians>");
	}
}
