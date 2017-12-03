package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author Source
 */
@AIName("siege_raceprotector")
public class SiegeRaceProtectorAI extends SiegeNpcAI {

	public SiegeRaceProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	public boolean ask(AIQuestion question) {
		switch (question) {
			case SHOULD_DECAY:
			case SHOULD_REWARD:
			case SHOULD_LOOT:
				return true;
			default:
				return super.ask(question);
		}
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP) {
			switch (getNpcId()) {
				case 235064: // empowered veille
				case 235065: // empowered mastarius
					stat.setBaseRate(SiegeConfig.SIEGE_HEALTH_MULTIPLIER);
			}
		}
	}
}
