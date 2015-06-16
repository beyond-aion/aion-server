package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author d3v1an
 */
public class Movie extends AdminCommand {

	public Movie() {
		super("movie");
	}

	@Override
	public void execute(Player player, String... params) {
		if (params.length < 1) {
			onFail(player, null);
		}
		else {
			PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(Integer.parseInt(params[0]), Integer.parseInt(params[1])));
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "//movie <type> <id>");
	}
}
