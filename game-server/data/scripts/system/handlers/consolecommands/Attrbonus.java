package consolecommands;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatSetFunction;
import com.aionemu.gameserver.model.stats.container.CreatureGameStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.chathandlers.ConsoleCommand;

/**
 * @author ginho1
 */
public class Attrbonus extends ConsoleCommand implements StatOwner {

	public Attrbonus() {
		super("attrbonus");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length < 1) {
			info(admin, null);
			return;
		}

		StatEnum stat = StatEnum.valueOf(StatEnum.class, String.valueOf(params[0]));

		if (stat == null) {
			PacketSendUtility.sendMessage(admin, "Invalid params.");
			return;
		}

		int value;

		try {
			value = Integer.parseInt(params[1]);
		} catch (NumberFormatException e) {
			PacketSendUtility.sendMessage(admin, "Invalid params.");
			return;
		}

		Creature effected = admin;
		CreatureGameStats<? extends Creature> cgs = effected.getGameStats();

		List<IStatFunction> modifiers = new ArrayList<>();

		modifiers.add(new StatSetFunction(stat, value));

		if (modifiers.size() > 0)
			cgs.addEffect(this, modifiers);

		PacketSendUtility.sendMessage(admin, "Character stat " + stat.name() + " increased.");
	}

	@Override
	public void info(Player admin, String message) {
		PacketSendUtility.sendMessage(admin, "syntax ///attrbonus <stat> <value>");
	}
}
