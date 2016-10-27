package ai;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author xTz, Neon
 */
@AIName("onedmg_aggressive")
public class OneDmgAI2 extends AggressiveNpcAI2 {

	@Override
	public int modifyDamage(Creature attacker, int damage, Effect effect) {
		return 1;
	}

	@Override
	public int modifyOwnerDamage(int damage) {
		return 1;
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		switch (stat.getStat()) { // ai owner should not evade or resist
			case MAGICAL_RESIST:
			case EVASION:
				stat.setBase(0);
				stat.setBonus(0);
		}
	}
}
