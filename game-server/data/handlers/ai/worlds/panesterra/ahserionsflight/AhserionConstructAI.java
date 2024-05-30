package ai.worlds.panesterra.ahserionsflight;

import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.poll.AIQuestion;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author Yeats, Estrayl
 */
public class AhserionConstructAI extends NpcAI {

	public AhserionConstructAI(Npc owner) {
		super(owner);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(SiegeConfig.AHSERION_MAX_PLAYERS_PER_TEAM / 100f);
	}

	@Override
	public float modifyDamage(Creature attacker, float damage, Effect effect) {
		if (attacker instanceof Npc && effect != null) {
			switch (effect.getSkillId()) {
				case 21755: // Bombarding targets.
				case 21578: // Shield Penetration
				case 21583: // Artillery Blast
				case 21584: // Area Bombardment
					return damage * (SiegeConfig.AHSERION_MAX_PLAYERS_PER_TEAM / 100f);
			}
		}
		return super.modifyDamage(attacker, damage, effect);
	}

	@Override
	public boolean ask(AIQuestion question) {
		return switch (question) {
			case ALLOW_RESPAWN, REWARD_LOOT -> false;
			default -> super.ask(question);
		};
	}
}
