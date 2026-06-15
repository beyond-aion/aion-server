package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * @author ATracer, Source
 */
@AIName("fortress_protector")
public class FortressProtectorNpcAI extends AbstractSiegeProtectorAI {

	public FortressProtectorNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	public float modifyOwnerDamage(float damage, Creature effected, Effect effect) {
		if (effected instanceof Npc)
			return damage * 5;
		return damage;
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP && getOwner().getRating() == NpcRating.LEGENDARY)
			stat.setBaseRate(SiegeConfig.FORTRESS_PROTECTOR_HEALTH_MULTIPLIER);
	}
}
