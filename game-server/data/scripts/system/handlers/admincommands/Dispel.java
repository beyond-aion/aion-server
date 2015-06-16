package admincommands;

import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Hilgert
 */
public class Dispel extends AdminCommand {

	public Dispel() {
		super("dispel");
	}

	@Override
	public void execute(Player admin, String... params) {
		Player target = null;
		VisibleObject creature = admin.getTarget();

		if (creature == null) {
			PacketSendUtility.sendMessage(admin, "You should select a target first!");
			return;
		}

		if (creature instanceof Player) {
			target = (Player) creature;
			target.getEffectController().removeAllEffects();
			PacketSendUtility.sendMessage(admin, creature.getName() + " had all buff effects dispelled !");
		}
	}

	@Override
	public void onFail(Player player, String message) {
		// TODO Auto-generated method stub
	}
}
