package admincommands;

import java.util.Iterator;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MEGAPHONE;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author ginho1
 */
public class Megaphone extends AdminCommand {

	public Megaphone() {
		super("megaphone");
	}

	@Override
	public void execute(Player player, String... params) {

		if (params.length < 1) {
			onFail(player, null);
			return;
		}

		String message = "";

		// Add with space
		for (int i = 0; i < params.length - 1; i++)
			message += params[i] + " ";

		// Add the last without the end space
		message += params[params.length - 1];

		Iterator<Player> iter = World.getInstance().getPlayersIterator();
		while (iter.hasNext()) {
			PacketSendUtility.sendPacket(iter.next(), new SM_MEGAPHONE(player.getName(), message, 188910000));
		}
	}

	@Override
	public void onFail(Player player, String message) {
		PacketSendUtility.sendMessage(player, "Syntax: //megaphone <message>");
	}
}
