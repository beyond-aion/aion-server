package admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.world.World;

/**
 * @author Rolandas
 */
public class MoveToObject extends AdminCommand {

	public MoveToObject() {
		super("movetoobj", "Teleports you to an object.", """
			<object id> - Teleports you to the current position of the object.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params == null || params.length != 1) {
			sendInfo(admin);
			return;
		}

		int objectId = Integer.parseInt(params[0]);

		VisibleObject object = World.getInstance().findVisibleObject(objectId);
		if (object == null) {
			sendInfo(admin, "There is no object with ID " + objectId + ".");
			return;
		}

		TeleportService.teleportTo(admin, object.getPosition());
	}

}
