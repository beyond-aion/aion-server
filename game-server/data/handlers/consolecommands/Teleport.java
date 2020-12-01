package consolecommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * Created by Yeats on 09.05.2016.
 */
public class Teleport extends ConsoleCommand {

	public Teleport() {
		super("teleport");
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length == 4) {
			int worldId = DataManager.WORLD_MAPS_DATA.getWorldIdByCName(params[0]);
			if (worldId != 0) {
				try {
					int x = Integer.valueOf(params[1]);
					int y = Integer.valueOf(params[2]);
					int z = Integer.valueOf(params[3]);
					TeleportService.teleportTo(player, worldId, x, y, z);
				} catch (NumberFormatException ex) {
				}
			}
		}
	}
}
