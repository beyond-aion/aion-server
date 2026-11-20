package admincommands;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Wakizashi
 */
public class AddExp extends AdminCommand {

	public AddExp() {
		super("addexp", "Increases/decreases a players experience points.", """
			<exp> - The experience points to add (may be negative), without any bonus or limit.
			<exp> <gather|craft|quest|hunt|group|pvp> - Grants the experience like the given source does: XP boost, Energy of Repose and Salvation, rates
			and the limit of a single reward. The numbers 0, 1, 2, 3 and 5 select gather, craft, quest, hunt and pvp.
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		Player target = admin.getTarget() instanceof Player p ? p : admin;
		long exp = Long.parseLong(params[0]);
		if (params.length > 1) {
			Rates rates = switch (params[1].toLowerCase()) {
				case "gather", "0" -> Rates.XP_GATHERING;
				case "craft", "1" -> Rates.XP_CRAFTING;
				case "quest", "2" -> Rates.XP_QUEST;
				case "hunt", "3" -> Rates.XP_HUNTING;
				case "group" -> Rates.XP_GROUP_HUNTING;
				case "pvp", "5" -> Rates.XP_PVP;
				default -> null;
			};
			if (rates == null || exp <= 0) {
				sendInfo(admin);
				return;
			}
			long before = target.getCommonData().getExp();
			target.getCommonData().addExp(exp, rates, "GM");
			sendInfo(admin, name(target) + " gained " + (target.getCommonData().getExp() - before) + " exp as " + rates + ".");
			return;
		}
		long resultExp = Math.max(0, target.getCommonData().getExp() + exp);
		target.getCommonData().setExp(resultExp);
		sendInfo(admin, "You added " + exp + " exp points to " + name(target) + ".");
	}
}
