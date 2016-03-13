package admincommands;

import java.util.Iterator;
import java.util.List;

import com.aionemu.gameserver.model.account.PlayerAccountData;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.model.team2.group.PlayerGroup;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;
import com.google.common.base.Predicate;

/**
 * @author lyahim
 * @modified antness
 */
public class PlayerInfo extends AdminCommand {

	public PlayerInfo() {
		super("playerinfo", "Shows information about a player.");
		setParamInfo("<player name> <loc|item|group|skills|legion|ap|chars|knownlist|visuallist>");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		Player target = World.getInstance().findPlayer(Util.convertName(params[0]));
		if (target == null) {
			sendInfo(admin, "Selected player is not online!");
			return;
		}

		sendInfo(admin,
			"\n[Info about " + target.getName() + "]\n-common: lv" + target.getLevel() + "(" + target.getCommonData().getExpShown() + " xp), "
				+ target.getRace() + ", " + target.getPlayerClass() + "\n-ip: " + target.getClientConnection().getIP() + "\n" + "-account name: "
				+ target.getClientConnection().getAccount().getName() + "\n");

		if (params.length < 2)
			return;

		if (params[1].equals("item")) {
			StringBuilder strbld = new StringBuilder("-items in inventory:\n");
			List<Item> items = target.getInventory().getItemsWithKinah();
			Iterator<Item> it = items.iterator();
			if (items.isEmpty())
				strbld.append("none\n");
			else
				while (it.hasNext()) {
					Item act = it.next();
					strbld.append("\t" + act.getItemCount() + "x " + ChatUtil.item(act.getItemId()) + "\n");
				}
			items.clear();
			items = target.getEquipment().getEquippedItems();
			it = items.iterator();
			strbld.append("-equipped items:\n");
			if (items.isEmpty())
				strbld.append("none\n");
			else
				while (it.hasNext()) {
					Item act = it.next();
					strbld.append("\t" + act.getItemCount() + "x " + ChatUtil.item(act.getItemId()) + "\n");
				}
			items = target.getWarehouse().getItemsWithKinah();
			it = items.iterator();
			strbld.append("-items in warehouse:\n");
			if (items.isEmpty())
				strbld.append("none\n");
			else
				while (it.hasNext()) {
					Item act = it.next();
					strbld.append("\t" + act.getItemCount() + "x " + ChatUtil.item(act.getItemId()) + "\n");
				}
			sendInfo(admin, strbld.toString());
		} else if (params[1].equals("group")) {
			final StringBuilder strbld = new StringBuilder("-group info:\n\tLeader: ");

			PlayerGroup group = target.getPlayerGroup2();
			if (group == null)
				sendInfo(admin, "-group info: no group");
			else {
				strbld.append(group.getLeader().getName() + "\n\tMembers:\n");
				group.applyOnMembers(new Predicate<Player>() {

					@Override
					public boolean apply(Player player) {
						strbld.append("\t\t" + player.getName() + "\n");
						return true;
					}
				});
				sendInfo(admin, strbld.toString());
			}
		} else if (params[1].equals("skills")) {
			StringBuilder strbld = new StringBuilder("-list of skills:\n");
			for (PlayerSkillEntry skill : target.getSkillList().getAllSkills())
				strbld.append("\tlevel " + skill.getSkillLevel() + " of " + skill.getSkillName() + "\n");
			sendInfo(admin, strbld.toString());
		} else if (params[1].equals("loc")) {
			String chatLink = ChatUtil.position(target.getName(), target.getPosition());
			sendInfo(admin, "- " + chatLink + "'s location:\n\t" + target.getPosition());
		} else if (params[1].equals("legion")) {
			Legion legion = target.getLegion();
			if (legion == null)
				sendInfo(admin, "-legion info: no legion");
			else {
				StringBuilder strbld = new StringBuilder();
				List<LegionMemberEx> legionmemblist = LegionService.getInstance().loadLegionMemberExList(legion, null);
				Iterator<LegionMemberEx> it = legionmemblist.iterator();
				strbld.append("-legion info:\n\tname: " + legion.getLegionName() + ", level: " + legion.getLegionLevel() + "\n\tmembers(online):\n");
				while (it.hasNext()) {
					LegionMemberEx act = it.next();
					strbld.append("\t\t" + act.getName() + "(" + ((act.isOnline() == true) ? "online" : "offline") + ")" + act.getRank().toString() + "\n");
				}
				sendInfo(admin, strbld.toString());
			}
		} else if (params[1].equals("ap")) {
			sendInfo(admin, "AP info about " + target.getName());
			sendInfo(admin, "Total AP = " + target.getAbyssRank().getAp());
			sendInfo(admin, "Total Kills = " + target.getAbyssRank().getAllKill());
			sendInfo(admin, "Today Kills = " + target.getAbyssRank().getDailyKill());
			sendInfo(admin, "Today AP = " + target.getAbyssRank().getDailyAP());
		} else if (params[1].equals("chars")) {
			sendInfo(admin, "Others characters of " + target.getName() + " (" + target.getClientConnection().getAccount().size() + ") :");
			Iterator<PlayerAccountData> data = target.getClientConnection().getAccount().iterator();
			while (data.hasNext()) {
				PlayerAccountData d = data.next();
				if (d != null && d.getPlayerCommonData() != null) {
					sendInfo(admin, d.getPlayerCommonData().getName());
				}
			}
		} else if (params[1].equals("knownlist")) {
			sendInfo(admin, "KnownList of " + target.getName());
			for (VisibleObject obj : target.getKnownList().getKnownObjects().values())
				sendInfo(admin, obj.getName() + " objectId:" + obj.getObjectId());
		} else if (params[1].equals("visuallist")) {
			sendInfo(admin, "VisualList of " + target.getName());
			for (VisibleObject obj : target.getKnownList().getVisibleObjects().values())
				sendInfo(admin, obj.getName() + " objectId:" + obj.getObjectId());
		} else {
			sendInfo(admin);
		}
	}
}
