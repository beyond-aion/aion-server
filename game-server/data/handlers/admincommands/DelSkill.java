package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.model.skill.PlayerSkillList;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author xTz
 */
public class DelSkill extends AdminCommand {

	public DelSkill() {
		super("delskill", "Removes skills (except stigma skills).", """
			<all|skillId> - Removes all skills or the skill from your target (or yourself).
			<player> <all|skillId> - Removes all skills or the skill from the online player.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1 || params.length > 2) {
			sendInfo(admin);
			return;
		}

		Player player;
		PlayerSkillList playerSkillList = null;
		String recipient = null;
		recipient = Util.convertName(params[0]);
		int skillId = 0;
		if (params.length == 2) {
			player = World.getInstance().getPlayer(recipient);
			if (player == null) {
				sendInfo(admin, "The specified player is not online.");
				return;
			}

			if ("all".startsWith(params[1]))
				playerSkillList = player.getSkillList();
			else {
				skillId = Integer.parseInt(params[1]);

				if (!check(admin, player, skillId))
					return;
			}
			apply(admin, player, skillId, playerSkillList);

		}
		if (params.length == 1) {
			player = admin.getTarget() instanceof Player target ? target : admin;

			if ("all".startsWith(params[0]))
				playerSkillList = player.getSkillList();
			else {
				skillId = Integer.parseInt(params[0]);

				if (!check(admin, player, skillId))
					return;
			}
			apply(admin, player, skillId, playerSkillList);
		}
	}

	private static boolean check(Player admin, Player player, int skillId) {
		if (!player.getSkillList().isSkillPresent(skillId)) {
			sendInfo(admin, "The player doesn't have this skill.");
			return false;
		}
		if (player.getSkillList().getSkillEntry(skillId).isStigmaSkill()) {
			sendInfo(admin, "You can't remove stigma skill.");
			return false;
		}
		return true;
	}

	public void apply(Player admin, Player player, int skillId, PlayerSkillList playerSkillList) {
		if (skillId != 0) {
			SkillLearnService.removeSkill(player, skillId);
			sendInfo(admin, "You have successfully deleted the specified skill.");
		} else {
			for (PlayerSkillEntry skillEntry : playerSkillList.getAllSkills()) {
				if (!skillEntry.isStigmaSkill()) {
					SkillLearnService.removeSkill(player, skillEntry.getSkillId());
				}
			}

			sendInfo(admin, "You have successfully deleted all skills (except stigma skills).");
		}

	}
}
