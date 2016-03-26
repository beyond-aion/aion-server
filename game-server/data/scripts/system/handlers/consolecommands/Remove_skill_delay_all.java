package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author Neon
 */
public class Remove_skill_delay_all extends ConsoleCommand {

	public Remove_skill_delay_all() {
		super("remove_skill_delay_all", "Enables/disables zero cooldown mode.");

		setParamInfo("<1|0> - Enable or disable skill cooldowns.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0) {
			sendInfo(player);
			return;
		}
		if (params[0].equals("1") && player.isCoolDownZero()) {
			sendInfo(player, "Cooldown time of all skills has been recovered.");
			player.setCoolDownZero(false);
		} else if (params[0].equals("0") && !player.isCoolDownZero()) {
			sendInfo(player, "Cooldown time of all skills is set to 0.");
			player.setCoolDownZero(true);
		}
	}
}
