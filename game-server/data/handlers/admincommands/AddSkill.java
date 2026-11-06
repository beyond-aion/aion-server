package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Phantom
 */
public class AddSkill extends AdminCommand {

	public AddSkill() {
		super("addskill", "Adds a skill to a player.", "<skillId> <skillLevel> - Adds the skill to your target (or yourself).");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length != 2) {
			sendInfo(player);
			return;
		}

		Player target = player.getTarget() instanceof Player p ? p : player;

		int skillId = 0;
		int skillLevel = 0;

		try {
			skillId = Integer.parseInt(params[0]);
			skillLevel = Integer.parseInt(params[1]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(player, "Parameters need to be an integer.");
			return;
		}

		target.getSkillList().addSkill(target, skillId, skillLevel);
		PacketSendUtility.sendMessage(player, "You have success add skill");
		if (!target.equals(player))
			PacketSendUtility.sendMessage(target, "You have acquire a new skill");
	}
}
