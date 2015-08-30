package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author ATracer
 * @modified By aionchs- Wylovech
 */
public class Morph extends AdminCommand {

	public Morph() {
		super("morph");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length != 1) {
			PacketSendUtility.sendMessage(admin, "syntax //morph <NPC Id | cancel> ");
			return;
		}

		Player target = admin;
		int param = 0;

		if (admin.getTarget() instanceof Player)
			target = (Player) admin.getTarget();

		if (!("cancel").startsWith(params[0].toLowerCase())) {
			try {
				param = Integer.parseInt(params[0]);

			}
			catch (NumberFormatException e) {
				PacketSendUtility.sendMessage(admin, "Parameter must be an integer, or cancel.");
				return;
			}
		}

		if ((param != 0 && param < 200000) || param > 298021) {
			PacketSendUtility.sendMessage(admin, "Something wrong with the NPC Id!");
			return;
		}

		target.getTransformModel().apply(param);

		if (param == 0) {
			if (target.equals(admin)) {
				PacketSendUtility.sendMessage(target, "Morph successfully cancelled.");
			}
			else {
				PacketSendUtility.sendMessage(target, "Your morph has been cancelled by " + admin.getName() + ".");
				PacketSendUtility.sendMessage(admin, "You have cancelled " + target.getName() + "'s morph.");
			}
		}
		else {
			if (target.equals(admin)) {
				PacketSendUtility.sendMessage(target, "Successfully morphed to npcId " + param + ".");
			}
			else {
				PacketSendUtility.sendMessage(target, admin.getName() + " morphs you into an NPC form.");
				PacketSendUtility.sendMessage(admin, "You morph " + target.getName() + " to npcId " + param + ".");
			}
		}
	}

	@Override
	public void info(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //morph <NPC Id | cancel> ");
	}
}
