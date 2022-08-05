package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.CustomPlayerState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Andy, Divinity
 */
public class Invul extends AdminCommand {

	public Invul() {
		super("invul", "Enables/disables invulnerability.");
	}

	@Override
	public void execute(Player player, String... params) {
		if (player.isInvulnerable()) {
			player.unsetCustomState(CustomPlayerState.INVULNERABLE);
			PacketSendUtility.sendMessage(player, "You are now mortal.");
		} else {
			player.setCustomState(CustomPlayerState.INVULNERABLE);
			sendInfo(player, ChatUtil.l10n(293440)); // Immune to all damage.
		}
	}
}
