package ai.siege;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.configs.main.SiegeConfig;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.SummonerAI;

/**
 * TODO: Aether Concentrators - currently not necessary since they are nearly impossible to use and need
 * about 100 player to be activated by default.
 * 
 * @author Estrayl
 */
@AIName("enraged_agent")
public class EnragedAgent extends SummonerAI {

	public EnragedAgent(Npc owner) {
		super(owner);
	}

	@Override
	public void onEffectEnd(Effect effect) {
		switch (effect.getSkillId()) {
			case 18704:
				ThreadPoolManager.getInstance()
					.schedule(() -> SkillEngine.getInstance().getSkill(getOwner(), 18705, 60, getAggroList().getMostHated()).useSkill(), 650);
				break;
		}
	}

	@Override
	public void modifyOwnerStat(Stat2 stat) {
		if (stat.getStat() == StatEnum.MAXHP)
			stat.setBaseRate(SiegeConfig.FORTRESS_PROTECTOR_HEALTH_MULTIPLIER);
	}
}
