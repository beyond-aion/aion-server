package consolecommands;

import com.aionemu.gameserver.configs.main.GSConfig;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1, Neon
 */
public class Levelup extends ConsoleCommand {

	public Levelup() {
		super("levelup", "Levels a player up.", """
			<value> - Levels your target up by the specified number of levels (defaults to your character, if no player is targeted).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			sendInfo(admin);
			return;
		}
		Player player = admin.getTarget() instanceof Player target ? target : admin;
		int newLevel = player.getLevel() + Integer.parseInt(params[0]);
		if (newLevel < 1 || newLevel > GSConfig.PLAYER_MAX_LEVEL) {
			sendInfo(admin, "Invalid level.");
			return;
		}
		player.getCommonData().setLevel(newLevel);
		sendInfo(admin, "Set " + name(player) + "'s level to " + player.getLevel());
	}
}
