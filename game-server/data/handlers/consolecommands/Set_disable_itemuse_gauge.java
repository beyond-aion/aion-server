package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

public class Set_disable_itemuse_gauge extends ConsoleCommand {

	public Set_disable_itemuse_gauge() {
		super("set_disable_itemuse_gauge", "Enables/disables the use time of your items.", """
			1 - Items are used instantly.
			0 - Items take their regular use time.
			""");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length == 0 || !params[0].equals("0") && !params[0].equals("1")) {
			sendInfo(player);
			return;
		}
		if (params[0].equals("1"))
			player.setCustomState(CustomPlayerState.NO_ITEM_USE_GAUGE_MODE);
		else
			player.unsetCustomState(CustomPlayerState.NO_ITEM_USE_GAUGE_MODE);
		sendInfo(player, "Disable item use gauge: [" + (params[0].equals("1") ? "On" : "Off") + "]");
	}
}
