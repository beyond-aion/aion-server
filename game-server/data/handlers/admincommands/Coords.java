package admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Estrayl
 */
public class Coords extends AdminCommand {

	public Coords() {
		super("coords", "Shows the target's current coordinates.");
	}

	@Override
	public void execute(Player admin, String... params) {
		VisibleObject target = admin.getTarget() == null ? admin : admin.getTarget();
		sendInfo(admin, name(target) + "'s position:\n" + target.getPosition().toCoordString().replace(", X:", "\nX:"));
	}
}
