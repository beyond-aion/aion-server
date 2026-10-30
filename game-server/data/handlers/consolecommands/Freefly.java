package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * Sent by the client from the GM panel whenever its {@code g_freefly} cvar is toggled. The server cannot read that cvar, so this is
 * the only way to keep the flight area checks in sync with what the client allows.
 */
public class Freefly extends ConsoleCommand {

	public Freefly() {
		super("freefly", "Toggles free flight outside of flight areas, mirroring the client's g_freefly cvar.", """
			1 - Free flight everywhere.
			0 - Flight only inside flight areas.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		String value = params.length == 0 ? "" : params[0];
		if (!value.equals("0") && !value.equals("1")) {
			sendInfo(player);
			return;
		}
		if (value.equals("1")) {
			player.setCustomState(CustomPlayerState.FREE_FLY_MODE);
			sendInfo(player, "Free fly has been enabled.");
		} else {
			player.unsetCustomState(CustomPlayerState.FREE_FLY_MODE);
			sendInfo(player, "Free fly has been disabled.");
		}
	}
}
