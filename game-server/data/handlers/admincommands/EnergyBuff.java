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
		super("energy", "Manages repose and salvation energy of your target (or yourself).", """
			<repose|salvation> info - Shows the current energy.
			<repose|salvation> add <points> - Adds energy points.
			<repose|salvation> reset - Resets the energy.
			refresh - Resends the stats info packet.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params == null || params.length < 1) {
			sendInfo(player);
			return;
		}

		Player targetPlayer = player.getTarget() instanceof Player target ? target : player;
		if (params[0].equals("refresh")) {
			PacketSendUtility.sendPacket(targetPlayer, new SM_STATS_INFO(targetPlayer));
			return;
		}

		if (params.length < 2 || (params[1].equals("add") && params.length < 3)) {
			sendInfo(player);
			return;
		}

		if (params[0].equals("repose")) {
			if (params[1].equals("info"))
				sendInfo(player, "Current EoR: " + targetPlayer.getCommonData().getCurrentReposeEnergy() + "\n Max EoR: "
					+ targetPlayer.getCommonData().getMaxReposeEnergy());
			else if (params[1].equals("add"))
				targetPlayer.getCommonData().addReposeEnergy(Long.parseLong(params[2]));
			else if (params[1].equals("reset"))
				targetPlayer.getCommonData().setCurrentReposeEnergy(0);
			else
				sendInfo(player);
		} else if (params[0].equals("salvation")) {
			if (params[1].equals("info"))
				sendInfo(player, "Current EoS: " + targetPlayer.getCommonData().getCurrentSalvationPercent());
			else if (params[1].equals("add"))
				targetPlayer.getCommonData().addSalvationPoints(Long.parseLong(params[2]));
			else if (params[1].equals("reset"))
				targetPlayer.getCommonData().resetSalvationPoints();
			else
				sendInfo(player);
		} else {
			sendInfo(player);
		}
	}

}
