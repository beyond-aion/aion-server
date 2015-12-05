package admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Estrayl
 */
public class Coords extends AdminCommand {

	public Coords() {
		super("coords");
	}

	@Override
	public void execute(Player admin, String... params) {
		VisibleObject target = admin.getTarget();
		if (target == null)
			return;
		PacketSendUtility.sendMessage(admin, 
			"MapID: " + target.getPosition().getMapId() +
			" / X: " + target.getPosition().getX() + 
			" / Y: " + target.getPosition().getY() + 
			" / Z: " + target.getPosition().getZ() + 
			" / Heading: " + target.getPosition().getHeading());
	}

}
