package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATUPDATE_EXP;
import com.aionemu.gameserver.network.aion.serverpackets.SM_STATS_INFO;
import com.aionemu.gameserver.services.player.SalvationEnergyService;
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
			playtime <total minutes> <minutes at level up> - Sets the play time used by Energy of Salvation and checks it at once.
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

		if (params[0].equals("playtime")) {
			if (params.length < 3) {
				sendInfo(player);
				return;
			}
			targetPlayer.getCommonData().resetPlayTime(Integer.parseInt(params[1]));
			targetPlayer.getCommonData().setLevelUpPlayTime(Integer.parseInt(params[2]));
			SalvationEnergyService.restart(targetPlayer);
			sendInfo(player, "Play time " + params[1] + " min, at level up " + params[2] + " min, Energy of Salvation checked.");
			return;
		}

		if (params.length < 2 || (params[1].equals("add") && params.length < 3)) {
			sendInfo(player);
			return;
		}

		if (params[0].equals("repose")) {
			PlayerCommonData pcd = targetPlayer.getCommonData();
			if (params[1].equals("info"))
				sendInfo(player, "Current EoR: " + pcd.getCurrentReposeEnergy() + "\n Max EoR: " + pcd.getMaxReposeEnergy());
			else if (params[1].equals("add")) {
				pcd.addReposeEnergy(Long.parseLong(params[2]));
				sendReposeEnergy(targetPlayer);
			} else if (params[1].equals("reset")) {
				pcd.setCurrentReposeEnergy(0);
				sendReposeEnergy(targetPlayer);
			} else
				sendInfo(player);
		} else if (params[0].equals("salvation")) {
			PlayerCommonData pcd = targetPlayer.getCommonData();
			if (params[1].equals("info"))
				sendInfo(player, "Current EoS: " + pcd.getSalvationEnergy() + " (" + pcd.getCurrentSalvationPercent()
					+ "% of gained XP, drop +" + pcd.getSalvationDropBonus() + "%)\n Play time: " + pcd.getPlayTime() + " min, at level up: "
					+ pcd.getLevelUpPlayTime() + " min");
			else if (params[1].equals("add")) {
				pcd.addSalvationPoints(Long.parseLong(params[2]));
				sendSalvationEnergy(targetPlayer);
			} else if (params[1].equals("reset")) {
				pcd.resetSalvationPoints();
				sendSalvationEnergy(targetPlayer);
			} else
				sendInfo(player);
		} else {
			sendInfo(player);
		}
	}

	/**
	 * The client shows Energy of Repose from the exp packet, so it must be resent after the value changes.
	 */
	private static void sendReposeEnergy(Player player) {
		PlayerCommonData pcd = player.getCommonData();
		PacketSendUtility.sendPacket(player, new SM_STATUPDATE_EXP(pcd.getExpShown(), pcd.getExpRecoverable(), pcd.getExpNeed(),
			pcd.getCurrentReposeEnergy(), pcd.getMaxReposeEnergy()));
	}

	/**
	 * The client shows Energy of Salvation from the stats packet, so it must be resent after the value changes.
	 */
	private static void sendSalvationEnergy(Player player) {
		PacketSendUtility.sendPacket(player, new SM_STATS_INFO(player));
	}

}
