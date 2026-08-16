package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Source
 */
public class EnergyBuff extends AdminCommand {

	public EnergyBuff() {
		super("energy");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			info(player, null);
			return;
		}

		Player targetPlayer = player.getTarget() instanceof Player target ? target : player;
		if (params[0].equals("repose")) {
			if (params[1].equals("info"))
				PacketSendUtility.sendMessage(player, "Current EoR: " + targetPlayer.getCommonData().getCurrentReposeEnergy() + "\n Max EoR: "
					+ targetPlayer.getCommonData().getMaxReposeEnergy());
			else if (params[1].equals("add"))
				targetPlayer.getCommonData().addReposeEnergy(Long.parseLong(params[2]));
			else if (params[1].equals("reset"))
				targetPlayer.getCommonData().setCurrentReposeEnergy(0);
		} else if (params[0].equals("salvation")) {
			if (params[1].equals("info"))
				PacketSendUtility.sendMessage(player, "Current EoS: " + targetPlayer.getCommonData().getCurrentSalvationPercent());
			else if (params[1].equals("add"))
				targetPlayer.getCommonData().addSalvationPoints(Long.parseLong(params[2]));
			else if (params[1].equals("reset"))
				targetPlayer.getCommonData().resetSalvationPoints();
		} else if (params[0].equals("refresh")) {
			PacketSendUtility.sendPacket(targetPlayer, new SM_STATS_INFO(targetPlayer));
		}
	}

	@Override
	public void info(Player player, String message) {
		String syntax = "//energy repose|salvation|refresh info|reset|add [points]";
		PacketSendUtility.sendMessage(player, syntax);
	}

}
