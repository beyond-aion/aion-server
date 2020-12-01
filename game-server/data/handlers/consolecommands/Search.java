package consolecommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_GM_SEARCH;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author ginho1
 */
public class Search extends ConsoleCommand {

	public Search() {
		super("search");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length > 0) {
			Player p = World.getInstance().getPlayer(params[0]);
			if (p != null)
				PacketSendUtility.sendPacket(admin, new SM_GM_SEARCH(p));
		}
	}
}
