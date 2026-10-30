package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

import admincommands.RemoveCd;

/**
 * @author Neon
 */
public class Remove_skill_delay_all extends ConsoleCommand {

	public Remove_skill_delay_all() {
		super("remove_skill_delay_all", "Enables/disables your skill cooldowns.", """
			1 - Enables your skill cooldowns.
			0 - Removes and disables your skill cooldowns.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0 || !params[0].equals("0") && !params[0].equals("1")) {
			sendInfo(player);
			return;
		}
		if (params[0].equals("1")) {
			player.unsetCustomState(CustomPlayerState.NO_SKILL_COOLDOWN_MODE);
			sendInfo(player, "Cooldown times of all skills have been recovered.");
		} else if (params[0].equals("0")) {
			RemoveCd.removeSkillCooldowns(player);
			player.setCustomState(CustomPlayerState.NO_SKILL_COOLDOWN_MODE);
			sendInfo(player, "Cooldown times of all skills have been disabled.");
		}
	}
}
