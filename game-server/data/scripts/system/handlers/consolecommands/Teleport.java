package consolecommands;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService2;
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
			int worldId = DataManager.WORLD_MAPS_DATA.getWorlIdByName(params[0]);
			if (worldId != 0) {
				int x = -1;
				int y = -1;
				int z = -1;
				try {
					x = Integer.valueOf(params[1]);
					y = Integer.valueOf(params[2]);
					z = Integer.valueOf(params[3]);
				} catch (NumberFormatException ex) {
				}

				if (x > -1 && y > -1 && z > -1) {
					TeleportService2.teleportTo(player, worldId, x, y, z);
				}
			}
		}
	}
}
