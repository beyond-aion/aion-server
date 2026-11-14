package admincommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Phantom
 */
public class AddSkill extends AdminCommand {

	public AddSkill() {
		super("addskill", "Adds a skill to a player.", """
			<skillId> <skillLevel> - Adds the skill to your target (or yourself).
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length != 2) {
			sendInfo(player);
			return;
		}

		Player target = player.getTarget() instanceof Player p ? p : player;

		int skillId = Integer.parseInt(params[0]);
		int skillLevel = Integer.parseInt(params[1]);
		if (DataManager.SKILL_DATA.getSkillTemplate(skillId) == null) {
			sendInfo(player, "There is no skill with ID " + skillId + ".");
			return;
		}
		if (skillLevel < 1) {
			sendInfo(player, "The skill level must be at least 1.");
			return;
		}

		if (!target.getSkillList().addSkill(target, skillId, skillLevel)) {
			sendInfo(player, "The player already has this skill at the same or a higher level.");
			return;
		}
		sendInfo(player, "The skill was added successfully.");
		if (!target.equals(player))
			sendInfo(target, "You have acquired a new skill.");
	}
}
