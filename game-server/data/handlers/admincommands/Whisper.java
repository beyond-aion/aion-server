package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class Whisper extends AdminCommand {

	public Whisper() {
		super("whisper", "Enables/disables incoming whispers.");

		setSyntaxInfo("<on|off> - Enable or disable whispers from others (GMs can always whisper you).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		if (params[0].equalsIgnoreCase("off")) {
			admin.setCustomState(CustomPlayerState.NO_WHISPERS_MODE);
			sendInfo(admin, "Accepting whispers: OFF");
		} else if (params[0].equalsIgnoreCase("on")) {
			admin.unsetCustomState(CustomPlayerState.NO_WHISPERS_MODE);
			sendInfo(admin, "Accepting whispers: ON");
		}
	}
}
