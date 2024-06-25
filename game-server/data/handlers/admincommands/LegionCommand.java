package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionRank;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author KID
 */
public class LegionCommand extends AdminCommand {

	public LegionCommand() {
		super("legion", "Modifies a legion.");

		// @formatter:off
		setSyntaxInfo(
			"info <legion name> - List legion members.",
			"add <legion name> <player name> - Adds the player to the legion.",
			"kick <player name> - Kicks the player from their legion.",
			"disband <legion name> - Disbands the legion.",
			"rename <legion name> <new name> - Changes the legion's name.",
			"setbg <legion name> <player name> - Changes a legion's brigade general.",
			"setlevel <legion name> <level> - Changes the legion's level.",
			"setpoints <legion name> <points> - Changes the legion's contributing points."
		);
		// @formatter:on
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 2) {
			sendInfo(player);
			return;
		}

		if (params[0].equalsIgnoreCase("disband")) {
			Legion legion = getLegion(params[1]);
			LegionService.getInstance().disbandLegion(legion);
			sendInfo(player, "Legion " + legion.getName() + " was disbanded.");
		} else if (params[0].equalsIgnoreCase("setlevel") && params.length >= 3) {
			Legion legion = getLegion(params[1]);
			int level = Integer.parseInt(params[2]);
			if (level < 1 || level > 8) {
				sendInfo(player, "Legion level must be between 1 and 8.");
				return;
			} else if (level == legion.getLegionLevel()) {
				sendInfo(player, "Legion " + params[1] + " is already on level " + level);
				return;
			}
			int old = legion.getLegionLevel();
			LegionService.getInstance().changeLevel(legion, level, true);
			sendInfo(player, "Legion " + legion.getName() + " level was changed from " + old + " to " + level);
		} else if (params[0].equalsIgnoreCase("setpoints") && params.length >= 3) {
			Legion legion = getLegion(params[1]);
			long points = Long.parseLong(params[2]);
			if (points < 1) {
				sendInfo(player, "Points must be larger than zero.");
				return;
			}
			long old = legion.getContributionPoints();
			LegionService.getInstance().setContributionPoints(legion, points, true);
			sendInfo(player, "Legion " + legion.getName() + " contribution points were changed from " + old + " to " + points);
		} else if (params[0].equalsIgnoreCase("rename") && params.length >= 3) {
			Legion legion = getLegion(params[1]);
			String old = legion.getName();
			if (LegionService.getInstance().tryRename(legion, params[2], player, null))
				sendInfo(player, "Legion " + old + " was renamed to " + legion.getName() + ".");
		} else if (params[0].equalsIgnoreCase("info")) {
			Legion legion = getLegion(params[1]);
			sendInfo(player, "Legion name: " + legion.getName());
			sendInfo(player, "Level: " + legion.getLegionLevel());
			sendInfo(player, "Contribution points: " + legion.getContributionPoints());
			sendInfo(player, "Members (" + legion.getLegionMembers().size() + "):");
			for (int memberId : legion.getLegionMembers()) {
				PlayerCommonData pcd = PlayerService.getOrLoadPlayerCommonData(memberId);
				String brigadeGeneralInfo = memberId == legion.getBrigadeGeneral() ? ", brigade general" : "";
				sendInfo(player, "\t" + pcd.getName() + " (lv " + pcd.getLevel() + " " + pcd.getPlayerClass() + brigadeGeneralInfo + ")");
			}
		} else if (params[0].equalsIgnoreCase("kick")) {
			Player target = World.getInstance().getPlayer(Util.convertName(params[1]));
			if (target == null)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(params[1]));
			else if (target.getLegionMember().getRank() == LegionRank.BRIGADE_GENERAL)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_BANISH_CAN_BANISH_MASTER());
			else if (LegionService.getInstance().leaveLegion(target, true)) 
				sendInfo(player, target.getName() + " was kicked from the legion.");
			else
				sendInfo(player, target.getName() + " could not be kicked from the legion.");
		} else if (params[0].equalsIgnoreCase("add") && params.length >= 3) {
			Legion legion = getLegion(params[1]);
			Player target = World.getInstance().getPlayer(Util.convertName(params[2]));
			if (target == null)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(params[2]));
			else if (target.isLegionMember())
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GUILD_INVITE_HE_IS_OTHER_GUILD_MEMBER(target.getName()));
			else if (LegionService.getInstance().addToLegion(legion, target, player))
				sendInfo(player, target.getName() + " was added to " + legion.getName());
		} else if (params[0].equalsIgnoreCase("setbg") && params.length >= 3) {
			Legion legion = getLegion(params[1]);
			Player target = World.getInstance().getPlayer(Util.convertName(params[2]));
			if (target == null) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(params[2]));
				return;
			}
			if (!legion.isMember(target.getObjectId())) {
				sendInfo(player, target.getName() + " is not a member of " + legion.getName());
				return;
			}
			LegionService.getInstance().appointBrigadeGeneral(target);
			sendInfo(player, "Legion brigade general changed to " + target.getName() + ".");
		} else {
			sendInfo(player);
		}
	}

	private Legion getLegion(String name) {
		if (name.contains("_"))
			name = name.replaceAll("_", " ");
		Legion legion = LegionService.getInstance().getLegion(name.toLowerCase());
		if (legion == null) {
			throw new IllegalArgumentException("Legion " + name + " does not exist.");
		}
		return legion;
	}
}
