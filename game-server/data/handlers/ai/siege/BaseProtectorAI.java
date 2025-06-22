package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;

/**
 * @author Estrayl
 */
@AIName("base_protector")
public class BaseProtectorAI extends SiegeNpcAI {

	public BaseProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP && getOwner().getLevel() >= 65) // Avoid adjusting low-level zones
			stat.setBaseRate(SiegeConfig.BASE_PROTECTOR_HEALTH_MULTIPLIER);
	}
}
