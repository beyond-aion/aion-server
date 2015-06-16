package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

public class Whisper extends AdminCommand {

	public Whisper() {
		super("whisper");
	}

	@Override
	public void execute(Player admin, String... params) {

		if(params[0].equalsIgnoreCase("off")) {
			admin.setUnWispable();
			PacketSendUtility.sendMessage(admin, "Accepting Whisper : OFF");
		}
		else if (params[0].equalsIgnoreCase("on")) {
			admin.setWispable();
			PacketSendUtility.sendMessage(admin, "Accepting Whisper : ON");
		}
	}	

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "syntax //whisper [on for wispable / off for unwispable]");
	}
}
