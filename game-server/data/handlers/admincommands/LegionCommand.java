package admincommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.dao.LegionMemberDAO;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.PlayerCommonData;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.model.team.legion.LegionRank;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_UPDATE_MEMBER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RENAME;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.services.NameRestrictionService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author KID
 */
public class LegionCommand extends AdminCommand {

	private LegionService service;

	public LegionCommand() {
		super("legion");
		service = LegionService.getInstance();
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			info(player, null);
			return;
		}

		if (params[0].equalsIgnoreCase("disband")) {
			if (!verifyLength(player, 2, params)) // legion disband NAME
				return;

			Legion legion = verifyLegionExists(player, params[1]);
			if (legion == null)
				return;

			service.disbandLegion(legion);
			PacketSendUtility.sendMessage(player, "legion " + legion.getName() + " was disbanded.");
		} else if (params[0].equalsIgnoreCase("setlevel")) {
			if (!verifyLength(player, 3, params)) // legion setlevel NAME level
				return;

			Legion legion = verifyLegionExists(player, params[1]);
			if (legion == null)
				return;

			byte level = -1;
			try {
				level = Byte.parseByte(params[2]);
			} catch (Exception e) {
			}

			if (level < 1 || level > 8) {
				PacketSendUtility.sendMessage(player, "1-8 legion level is allowed.");
				return;
			} else if (level == legion.getLegionLevel()) {
				PacketSendUtility.sendMessage(player, "legion " + params[1] + " is already with that level.");
				return;
			}

			int old = legion.getLegionLevel();
			service.changeLevel(legion, level, true);
			PacketSendUtility.sendMessage(player, "legion " + legion.getName() + " has raised from " + old + " to " + level + " level.");
		} else if (params[0].equalsIgnoreCase("setpoints")) {
			if (!verifyLength(player, 3, params)) // legion setpoints NAME points
				return;

			Legion legion = verifyLegionExists(player, params[1]);
			if (legion == null)
				return;

			long points = -1;
			try {
				points = Long.parseLong(params[2]);
			} catch (Exception e) {
			}

			if (points < 1 || points > Long.MAX_VALUE) {
				PacketSendUtility.sendMessage(player, "1-2.1bil points allowed.");
				return;
			}

			long old = legion.getContributionPoints();
			service.setContributionPoints(legion, points, true);
			PacketSendUtility.sendMessage(player, "legion " + legion.getName() + " has raised from " + old + " to " + points
				+ " contributiong points.");
		} else if (params[0].equalsIgnoreCase("setname")) {
			if (!verifyLength(player, 3, params)) // legion setname NAME NEWNAME
				return;

			Legion legion = verifyLegionExists(player, params[1]);
			if (legion == null)
				return;

			if (!NameRestrictionService.isValidLegionName(params[2])) {
				PacketSendUtility.sendMessage(player, params[2] + " is incorrect for legion name!");
				return;
			}
			String old = legion.getName();
			legion.setName(params[2]);
			LegionDAO.storeLegion(legion);
			PacketSendUtility.broadcastToWorld(new SM_RENAME(legion, old));
			PacketSendUtility.sendMessage(player, "Legion " + old + " has changed name to " + legion.getName() + ".");
		} else if (params[0].equalsIgnoreCase("info")) {
			if (!verifyLength(player, 2, params)) // legion info NAME
				return;

			Legion legion = verifyLegionExists(player, params[1]);
			if (legion == null)
				return;

			List<String> message = new ArrayList<>(), online = new ArrayList<>(), offline = new ArrayList<>();
			message.add("name: " + legion.getName());
			message.add("contrib points: " + legion.getContributionPoints());
			message.add("level: " + legion.getLegionLevel());
			message.add("id: " + legion.getLegionId());
			Collection<Integer> members = legion.getLegionMembers();
			message.add("members: " + members.size());

			for (int memberId : members) {
				PlayerCommonData pcd = PlayerService.getOrLoadPlayerCommonData(memberId);
				offline.add(pcd.getName() + " (lv" + pcd.getLevel() + ") classId " + pcd.getPlayerClass().getClassId());
			}

			message.add("--ONLINE-------- " + online.size());
			message.addAll(online);

			message.add("--OFFLINE-------- " + offline.size());
			message.addAll(offline);

			for (String msg : message)
				PacketSendUtility.sendMessage(player, msg);

		} else if (params[0].equalsIgnoreCase("kick")) {
			if (!verifyLength(player, 2, params)) // legion kick PLAYER
				return;

			Player target = World.getInstance().getPlayer(Util.convertName(params[1]));
			if (target == null) {
				PacketSendUtility.sendMessage(player, "player " + params[1] + " not exists.");
				return;
			} else if (target.getLegionMember().getRank() == LegionRank.BRIGADE_GENERAL) {
				PacketSendUtility.sendMessage(player, "player " + target.getName() + " is a brigade general. Disband legion!");
				return;
			}

			if (service.leaveLegion(target, true))
				PacketSendUtility.sendMessage(player, "player " + target.getName() + " was kicked from legion.");
			else
				PacketSendUtility.sendMessage(player, "You have failed to kick player " + target.getName() + " from legion.");
		} else if (params[0].equalsIgnoreCase("invite")) {
			if (!verifyLength(player, 3, params)) // legion invite NAME PLAYER
				return;

			Legion legion = verifyLegionExists(player, params[1]);
			if (legion == null)
				return;

			Player target = World.getInstance().getPlayer(Util.convertName(params[2]));
			if (target == null) {
				PacketSendUtility.sendMessage(player, "player " + params[2] + " not exists.");
				return;
			}

			if (target.isLegionMember()) {
				PacketSendUtility.sendMessage(player, "player " + target.getName() + " is a already member of " + target.getLegion().getName() + "!");
				return;
			}

			if (service.addToLegion(legion, target, player)) {
				PacketSendUtility.sendMessage(player, "player " + target.getName() + " was added to " + legion.getName());
			}
		} else if (params[0].equalsIgnoreCase("bg")) {
			if (!verifyLength(player, 3, params)) // legion bg NAME PLAYER
				return;

			Legion legion = verifyLegionExists(player, params[1]);
			if (legion == null)
				return;

			Player target = World.getInstance().getPlayer(Util.convertName(params[2]));
			if (target == null) {
				PacketSendUtility.sendMessage(player, "player " + params[2] + " not exists.");
				return;
			}

			if (!legion.isMember(target.getObjectId())) {
				PacketSendUtility.sendMessage(player, "player " + target.getName() + " is not a member of " + legion.getName() + ", invite them!");
				return;
			}

			Collection<Integer> members = legion.getLegionMembers();
			for (int memberId : members) {
				Player pl = World.getInstance().getPlayer(memberId);
				if (pl != null) {
					if (pl.getLegionMember().getRank() == LegionRank.BRIGADE_GENERAL) {
						pl.getLegionMember().setRank(LegionRank.LEGIONARY);
						PacketSendUtility.broadcastToLegion(target.getLegion(), new SM_LEGION_UPDATE_MEMBER(pl, 0, ""));
						PacketSendUtility.sendMessage(player, "You have sucessfully demoted " + pl.getName() + " to Legionary rank.");
						break;
					}
				} else {
					LegionMemberEx member = LegionService.getInstance().getLegionMemberEx(memberId);
					if (member.getRank() == LegionRank.BRIGADE_GENERAL) {
						LegionMember bgPlayer = LegionService.getInstance().getLegionMember(member.getObjectId());
						bgPlayer.setRank(LegionRank.LEGIONARY);
						member.setRank(LegionRank.LEGIONARY);
						PacketSendUtility.broadcastToLegion(target.getLegion(), new SM_LEGION_UPDATE_MEMBER(member, 0, ""));
						PacketSendUtility.sendMessage(player, "You have sucessfully demoted " + member.getName() + " to Legionary rank.");
						LegionMemberDAO.storeLegionMember(memberId, bgPlayer);
						break;
					}

				}
			}

			target.getLegionMember().setRank(LegionRank.BRIGADE_GENERAL);
			PacketSendUtility.broadcastToLegion(target.getLegion(), new SM_LEGION_UPDATE_MEMBER(target, 0, ""));
			PacketSendUtility.sendMessage(player, "You have sucessfully promoted " + target.getName() + " to BG rank.");
		} else if (params[0].equalsIgnoreCase("help")) {
			this.info(player, null);
		} else if (params[0].equalsIgnoreCase("setrank")) {
			if (!verifyLength(player, 3, params)) // legion setrank PLAYER RANK
				return;

			Player target = World.getInstance().getPlayer(Util.convertName(params[1]));
			if (target == null) {
				PacketSendUtility.sendMessage(player, "player " + params[1] + " not exists.");
				return;
			}

			if (!target.isLegionMember()) {
				PacketSendUtility.sendMessage(player, "player " + target.getName() + " is not a member of legion.");
				return;
			}

			if (params[2].equalsIgnoreCase("centurion")) {
				target.getLegionMember().setRank(LegionRank.CENTURION);
				PacketSendUtility.broadcastToLegion(target.getLegion(), new SM_LEGION_UPDATE_MEMBER(target, 0, ""));
				PacketSendUtility.sendMessage(player, "you have promoted player " + target.getName() + " as centurion.");
			} else if (params[2].equalsIgnoreCase("deputy")) {
				target.getLegionMember().setRank(LegionRank.DEPUTY);
				PacketSendUtility.broadcastToLegion(target.getLegion(), new SM_LEGION_UPDATE_MEMBER(target, 0, ""));
				PacketSendUtility.sendMessage(player, "you have promoted player " + target.getName() + " as deputy.");
			} else if (params[2].equalsIgnoreCase("legionary")) {
				target.getLegionMember().setRank(LegionRank.LEGIONARY);
				PacketSendUtility.broadcastToLegion(target.getLegion(), new SM_LEGION_UPDATE_MEMBER(target, 0, ""));
				PacketSendUtility.sendMessage(player, "you have promoted player " + target.getName() + " as legionary.");
			} else if (params[2].equalsIgnoreCase("volunteer")) {
				target.getLegionMember().setRank(LegionRank.VOLUNTEER);
				PacketSendUtility.broadcastToLegion(target.getLegion(), new SM_LEGION_UPDATE_MEMBER(target, 0, ""));
				PacketSendUtility.sendMessage(player, "you have promoted player " + target.getName() + " as volunteer.");
			} else {
				PacketSendUtility.sendMessage(player, "rank " + params[2] + " is not supported.");
			}
		}
	}

	private Legion verifyLegionExists(Player player, String name) {
		if (name.contains("_"))
			name = name.replaceAll("_", " ");
		Legion legion = service.getLegion(name.toLowerCase());
		if (legion == null) {
			PacketSendUtility.sendMessage(player, "legion " + name + " not exists.");
			return null;
		}
		return legion;
	}

	private boolean verifyLength(Player player, int size, String... cmd) {
		boolean ok = cmd.length >= size;
		if (!ok)
			this.info(player, size + " parameters required for element //legion " + cmd[0] + ".");

		return ok;
	}

	@Override
	public void info(Player player, String message) {
		if (message != null)
			PacketSendUtility.sendMessage(player, "FailReason: " + message);

		PacketSendUtility.sendMessage(player, "//legion info <legion name> : get list of legion members");
		PacketSendUtility.sendMessage(player, "//legion bg <legion name> <new bg name> : set a new brigade general to the legion");
		PacketSendUtility.sendMessage(player, "//legion kick <player name> : kick player to this legion");
		PacketSendUtility.sendMessage(player, "//legion invite <legion name> <player name> : add player to legion");
		PacketSendUtility.sendMessage(player, "//legion disband <legion name> : disbands legion");
		PacketSendUtility.sendMessage(player, "//legion setlevel <legion name> <level> : sets legion level");
		PacketSendUtility.sendMessage(player, "//legion setpoints <legion name> <points> : set contributing points");
		PacketSendUtility.sendMessage(player, "//legion setname <legion name> <new name> : change legion name");
	}
}
