package admincommands;

import java.util.List;
import java.util.stream.Collectors;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.team.legion.LegionMember;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
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
		super("playerinfo", "Shows information about a player.", """
			<player name> - Shows basic information about the given player.
			<player name> <item|party|skills|legion|ap|chars|knownlist> - Shows extended information about the given player.
			""");
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
			"[Info about " + name(target) + "]\n- Common: lv" + target.getLevel() + " (" + target.getCommonData().getExpShown() + " xp), "
				+ target.getRace() + ", " + target.getPlayerClass() + "\n- IP: " + target.getClientConnection().getIP() + "\n" + "- Account name: "
				+ target.getAccount().getName() + "\n- " + ChatUtil.position("Location", target.getPosition()) + ": " + target.getPosition().toCoordString());

		if (params.length < 2)
			return;

		if (params[1].equalsIgnoreCase("item")) {
			StringBuilder strbld = new StringBuilder("- Items in inventory:");
			appendItems(strbld, target.getInventory().getItemsWithKinah());
			strbld.append("\n- Equipped items:");
			appendItems(strbld, target.getEquipment().getEquippedItems());
			strbld.append("\n- Items in warehouse:");
			appendItems(strbld, target.getWarehouse().getItemsWithKinah());
			sendInfo(admin, strbld.toString());
		} else if (params[1].equalsIgnoreCase("party")) {
			StringBuilder sb = new StringBuilder("- Party: ");
			var team = target.getCurrentTeam();
			if (team == null) {
				sb.append("none");
			} else {
				sb.append(team.getClass().getSimpleName().replace("Player", ""));
				sb.append("\n\tLeader: ").append(name(team.getLeaderObject())).append("\n\tMembers:\n");
				team.forEach(player -> sb.append("\t").append(name(player)).append("\n"));
			}
			sendInfo(admin, sb.toString());
		} else if (params[1].equalsIgnoreCase("skills")) {
			StringBuilder sb = new StringBuilder("- Skills:");
			for (PlayerSkillEntry skill : target.getSkillList().getAllSkills())
				sb.append("\n\tlevel " + skill.getSkillLevel() + " of " + skill.getSkillTemplate().getL10n());
			sendInfo(admin, sb.toString());
		} else if (params[1].equalsIgnoreCase("legion")) {
			Legion legion = target.getLegion();
			if (legion == null)
				sendInfo(admin, "- Legion: none");
			else {
				StringBuilder sb = new StringBuilder("- Legion: \"" + legion.getName() + "\", level: " + legion.getLegionLevel());
				sb.append("\n\t").append(legion.getMembers().size()).append(" members:");
				for (LegionMember lm : legion.getMembers())
					sb.append("\n\t").append(lm.getName()).append(" - ").append(lm.getRank()).append(lm.isOnline() ? " (online)" : "");
				sendInfo(admin, sb.toString());
			}
		} else if (params[1].equalsIgnoreCase("ap")) {
			sendInfo(admin, "- AP info:");
			sendInfo(admin, "\tTotal AP = " + target.getAbyssRank().getAp());
			sendInfo(admin, "\tTotal Kills = " + target.getAbyssRank().getAllKill());
			sendInfo(admin, "\tToday Kills = " + target.getAbyssRank().getDailyKill());
			sendInfo(admin, "\tToday AP = " + target.getAbyssRank().getDailyAP());
		} else if (params[1].equalsIgnoreCase("chars")) {
			sendInfo(admin, "- Characters (" + target.getAccount().size() + "):");
			target.getAccount().forEach(d -> sendInfo(admin, "\t" + d.getPlayerCommonData().getName()));
		} else if (params[1].equalsIgnoreCase("knownlist")) {
			sendInfo(admin, "- KnownList:" + target.getKnownList().stream().map(o -> "\n\t" + o).collect(Collectors.joining()));
		} else {
			sendInfo(admin);
		}
	}

	private void appendItems(StringBuilder strbld, List<Item> items) {
		if (items.isEmpty())
			strbld.append("\nnone");
		else
			items.forEach(item -> strbld.append("\n").append(ChatUtil.leftPad(item.getItemCount(), 4)).append("x ")
				.append(ChatUtil.item(item.getItemId())));
	}
}
