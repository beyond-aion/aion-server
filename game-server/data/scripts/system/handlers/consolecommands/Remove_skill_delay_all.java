package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author Neon
 */
public class Remove_skill_delay_all extends ConsoleCommand {

	public Remove_skill_delay_all() {
		super("remove_skill_delay_all", "Enables/disables zero cooldown mode for all skills.");

		setSyntaxInfo("<1|0> - Enable or disable skill cooldowns.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}
		if (params[0].equals("1") && player.isInCustomState(CustomPlayerState.NO_SKILL_COOLDOWN_MODE)) {
			sendInfo(player, "Cooldown times of all skills have been recovered.");
			player.unsetCustomState(CustomPlayerState.NO_SKILL_COOLDOWN_MODE);
		} else if (params[0].equals("0") && !player.isInCustomState(CustomPlayerState.NO_SKILL_COOLDOWN_MODE)) {
			sendInfo(player, "Cooldown times of all skills have been disabled.");
			player.setCustomState(CustomPlayerState.NO_SKILL_COOLDOWN_MODE);
		}
	}
}
