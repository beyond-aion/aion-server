package admincommands;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;

import admincommands.Stat.CommandStatFunction;

/**
 * @author ATracer, Neon
 */
public class Speed extends AdminCommand implements StatOwner {

	public Speed() {
		super("speed", "Sets your speed.", """
			<0-100> - Set your speed to the specified value (0 to reset).
			""");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}
		float parameter = Float.parseFloat(params[0]);
		if (parameter < 0 || parameter > 100) {
			sendInfo(admin, "Speed must be between 0 and 100.");
			return;
		}
		admin.getGameStats().endEffect(this);
		if (parameter == 0) {
			sendInfo(admin, "Your regular speed has been restored.");
			return;
		}
		int speed = (int) (parameter * 1000);
		List<IStatFunction> functions = List.of(new CommandStatFunction(StatEnum.SPEED, speed), new CommandStatFunction(StatEnum.FLY_SPEED, speed));
		admin.getGameStats().addEffect(this, functions);
		sendInfo(admin, "Your speed is now fixed at " + parameter + ".");
	}
}
