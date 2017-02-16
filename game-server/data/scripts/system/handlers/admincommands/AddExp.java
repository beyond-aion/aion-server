package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Wakizashi
 */
public class AddExp extends AdminCommand {

	public AddExp() {
		super("addexp", "Increases/decreases a players experience points.");

		setSyntaxInfo("<exp> - The experience points to add (may be negative).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		Player target = admin;

		if (admin.getTarget() instanceof Player)
			target = (Player) admin.getTarget();

		long exp;
		try {
			exp = Long.parseLong(params[0]);
		} catch (NumberFormatException e) {
			sendInfo(admin, "Invalid <exp> (must be a number)");
			return;
		}

		long resultExp = Math.max(0, target.getCommonData().getExp() + exp);
		target.getCommonData().setExp(resultExp);
		sendInfo(admin, "You added " + exp + " exp points to " + target.getName() + ".");
	}
}
