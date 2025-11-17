package ai.worlds.inggison;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.model.Effect;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("sematariux_thunder_shield")
public class SematariuxThunderShieldAI extends AggressiveNpcAI {

	public SematariuxThunderShieldAI(Npc owner) {
		super(owner);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(0.1f);
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		return damage * 0.5f;
	}
}
