package admincommands;

import java.util.Arrays;
import java.util.EnumSet;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.functions.StatSetFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

/**
 * @author Estrayl
 */
public class AlterNpc extends AdminCommand {

	private static final EnumSet<StatEnum> allowedStats = EnumSet.of(StatEnum.MAXHP, StatEnum.PHYSICAL_ATTACK, StatEnum.MAGICAL_ATTACK,
		StatEnum.PHYSICAL_DEFENSE, StatEnum.MAGICAL_DEFEND, StatEnum.MAGICAL_RESIST, StatEnum.PARRY, StatEnum.PHYSICAL_ACCURACY,
		StatEnum.MAGICAL_ACCURACY, StatEnum.PHYSICAL_CRITICAL_RESIST, StatEnum.MAGIC_SKILL_BOOST_RESIST);

	public AlterNpc() {
		super("alternpc", "Used to alter stats of an NPC.");

		// @formatter:off
		setSyntaxInfo(
			"<list> - Displays all alterable stats.",
			"<change> [stat] [value] - Changes the respective stat to given value."
		);
		// @formatter:on
	}

	@Override
	protected void execute(Player player, String... params) {
		if (params.length < 1) {
			sendInfo(player);
			return;
		}
		if (params[0].equalsIgnoreCase("list"))
			showList(player);
		else if (params[0].equalsIgnoreCase("change") && params.length > 2)
			changeStat(player, params);
		else
			sendInfo(player);
	}

	private void changeStat(Player player, String[] params) {
		StatEnum toModify = StatEnum.valueOf(params[1].toUpperCase());
		if (!allowedStats.contains(toModify)) {
			sendInfo(player, "'" + params[0] + "' is not supported.");
			return;
		}

		if (!(player.getTarget() instanceof Npc)) {
			sendInfo(player, "You should select an NPC first.");
			return;
		}
		Npc target = (Npc) player.getTarget();
		int newValue = parseValue(params[2]);
		if (newValue < 1) {
			sendInfo(player, "New stat values have to be numbers.");
			return;
		}

		target.getGameStats().addEffect(null, Arrays.asList(new StatSetFunction(toModify, newValue)));
		if (toModify == StatEnum.MAXHP)
			target.getLifeStats().setCurrentHp(newValue);
		PacketSendUtility.sendMessage(player, "Altered " + target + "'s " + toModify.toString() + " to " + params[2] + ".");
	}

	private void showList(Player player) {
		String msg = "";
		for (StatEnum se : allowedStats)
			msg += "\n" + se.toString();
		PacketSendUtility.sendMessage(player, "These stats are currently allowed to change: " + msg);
	}

	private int parseValue(String value) {
		try {
			return Integer.parseInt(value.replace("_", ""));
		} catch (NumberFormatException e) {
			return 0;
		}
	}
}
