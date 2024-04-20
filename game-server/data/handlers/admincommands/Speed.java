package admincommands;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatOwner;
import com.aionemu.gameserver.model.stats.calc.functions.IStatFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.utils.chathandlers.AdminCommand;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author ATracer, Neon
 */
public class Speed extends AdminCommand implements StatOwner {

	public Speed() {
		super("speed", "Sets your speed.");

		setSyntaxInfo("<0-100> - Set your speed to the specified value (0 to reset).");
	}

	@Override
	public void execute(Player admin, String... params) {
		if (params.length == 0) {
			sendInfo(admin);
			return;
		}

		float parameter = 0;
		try {
			parameter = Float.parseFloat(params[0]);
			if (parameter < 0 || parameter > 100) {
				throw new IllegalArgumentException("Speed must be between 0 and 100.");
			}
		} catch (IllegalArgumentException e) {
			sendInfo(admin, e.getClass() == IllegalArgumentException.class ? e.getMessage() : null); // default info for NumberFormatException
			return;
		}

		admin.getGameStats().endEffect(this);
		if (parameter == 0) {
			sendInfo(admin, "Your standard speed has been recovered.");
			return;
		}

		List<IStatFunction> functions = new ArrayList<>();
		functions.add(new SpeedFunction(StatEnum.SPEED, parameter));
		functions.add(new SpeedFunction(StatEnum.FLY_SPEED, parameter));
		admin.getGameStats().addEffect(this, functions);
		sendInfo(admin, "Your speed is now fixed at " + parameter);
	}

	class SpeedFunction extends StatFunction {

		private int speed;

		SpeedFunction(StatEnum stat, float speed) {
			this.stat = stat;
			this.speed = (int) (speed * 1000);
		}

		@Override
		public void apply(Stat2 otherStat, CalculationType... calculationTypes) {
			otherStat.setBase(speed);
			otherStat.setBaseRate(1);
			otherStat.setBonus(0);
		}

		@Override
		public int getPriority() {
			return 120;
		}
	}
}
