package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GM_BOOKMARK_ADD;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Bookmark_add extends ConsoleCommand {

	public Bookmark_add() {
		super("Bookmark_add");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length > 0) {
			String bookmark_name = "";
			for (String string : params) {
				bookmark_name += string + " ";
			}
			if (!bookmark_name.isEmpty()) {
				bookmark_name = bookmark_name.trim();
				PacketSendUtility.sendPacket(admin, new SM_GM_BOOKMARK_ADD(bookmark_name, admin.getWorldId(), admin.getX(), admin.getY(), admin.getZ()));
			}
		}
	}
}
