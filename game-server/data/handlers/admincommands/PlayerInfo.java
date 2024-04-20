package admincommands;

import java.util.Iterator;
import java.util.List;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMemberEx;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author lyahim, antness
 */
public class PlayerInfo extends AdminCommand {

	public PlayerInfo() {
		super("playerinfo", "Shows information about a player.");
		setSyntaxInfo("<player name> <loc|item|group|skills|legion|ap|chars|knownlist|visuallist>");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		String playerName = Util.convertName(params[0]);
		Player target = World.getInstance().getPlayer(playerName);
		if (target == null) {
			PacketSendUtility.sendPacket(admin, SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
			return;
		}

		sendInfo(admin,
			"\n[Info about " + target.getName() + "]\n-common: lv" + target.getLevel() + "(" + target.getCommonData().getExpShown() + " xp), "
				+ target.getRace() + ", " + target.getPlayerClass() + "\n-ip: " + target.getClientConnection().getIP() + "\n" + "-account name: "
				+ target.getClientConnection().getAccount().getName() + "\n");

		if (params.length < 2)
			return;

		if (params[1].equals("item")) {
			StringBuilder strbld = new StringBuilder("-items in inventory:");
			appendItems(strbld, target.getInventory().getItemsWithKinah());
			strbld.append("-equipped items:");
			appendItems(strbld, target.getEquipment().getEquippedItems());
			strbld.append("-items in warehouse:");
			appendItems(strbld, target.getWarehouse().getItemsWithKinah());
			sendInfo(admin, strbld.toString());
		} else if (params[1].equals("group")) {
			final StringBuilder strbld = new StringBuilder("-group info:\n\tLeader: ");

			PlayerGroup group = target.getPlayerGroup();
			if (group == null)
				sendInfo(admin, "-group info: no group");
			else {
				strbld.append(group.getLeader().getName() + "\n\tMembers:\n");
				group.forEach(player -> strbld.append("\t\t" + player.getName() + "\n"));
				sendInfo(admin, strbld.toString());
			}
		} else if (params[1].equals("skills")) {
			StringBuilder strbld = new StringBuilder("-list of skills:\n");
			for (PlayerSkillEntry skill : target.getSkillList().getAllSkills())
				strbld.append("\tlevel " + skill.getSkillLevel() + " of " + DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId()).getName() + "\n");
			sendInfo(admin, strbld.toString());
		} else if (params[1].equals("loc")) {
			String chatLink = ChatUtil.position(target.getName(), target.getPosition());
			sendInfo(admin, "- " + chatLink + "'s location:\n\t" + target.getPosition().toCoordString());
		} else if (params[1].equals("legion")) {
			Legion legion = target.getLegion();
			if (legion == null)
				sendInfo(admin, "-legion info: no legion");
			else {
				StringBuilder strbld = new StringBuilder();
				List<LegionMemberEx> legionmemblist = LegionService.getInstance().loadLegionMemberExList(legion, null);
				Iterator<LegionMemberEx> it = legionmemblist.iterator();
				strbld.append("-legion info:\n\tname: " + legion.getName() + ", level: " + legion.getLegionLevel() + "\n\tmembers(online):\n");
				while (it.hasNext()) {
					LegionMemberEx act = it.next();
					strbld.append("\t\t" + act.getName() + "(" + (act.isOnline() ? "online" : "offline") + ")" + act.getRank().toString() + "\n");
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
			target.getClientConnection().getAccount().forEach(d -> sendInfo(admin, d.getPlayerCommonData().getName()));
		} else if (params[1].equals("knownlist")) {
			sendInfo(admin, "KnownList of " + target.getName());
			target.getKnownList().forEachObject(obj -> sendInfo(admin, obj.getName() + " objectId:" + obj.getObjectId()));
		} else if (params[1].equals("visuallist")) {
			sendInfo(admin, "VisualList of " + target.getName());
			target.getKnownList().forEachVisibleObject(obj -> sendInfo(admin, obj.getName() + " objectId:" + obj.getObjectId()));
		} else {
			sendInfo(admin);
		}
	}

	private void appendItems(StringBuilder strbld, List<Item> items) {
		if (items.isEmpty())
			strbld.append("\nnone");
		else
			items.forEach(item -> strbld.append("\n\t" + item.getItemCount() + "x " + ChatUtil.item(item.getItemId())));
	}
}
