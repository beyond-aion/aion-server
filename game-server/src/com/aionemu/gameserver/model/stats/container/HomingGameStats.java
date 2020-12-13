package com.aionemu.gameserver.model.stats.container;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Homing;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.stats.CalculationType;

/**
 * @author Cheatkiller
 */
public class HomingGameStats extends SummonedObjectGameStats {

	public HomingGameStats(Npc owner) {
		super(owner);
	}

	@Override
	public Stat2 getStat(StatEnum statEnum, float base, CalculationType... calculationTypes) {
		Stat2 stat = super.getStat(statEnum, base, calculationTypes);
		if (owner.getMaster() == null)
			return stat;
		switch (statEnum) {
			case MAGICAL_ATTACK:
				stat.setBonusRate(0.2f);
				return owner.getMaster().getGameStats().getItemStatBoost(statEnum, stat);

		}
		return stat;
	}

	@Override
	public Stat2 getMainHandMAttack(CalculationType... calculationTypes) {
		Homing homing = (Homing) owner;
		int power = getStatsTemplate().getMagicalAttack();
		SkillTemplate skill = DataManager.SKILL_DATA.getSkillTemplate(homing.getSkillId());
		int skillLvl = skill.getLvl();
		if (homing.getName().equals("gryphu"))
			power = 324;
		switch (skillLvl) {
			case 3:
				if (homing.getName().equals("stone energy"))
					power = 316;
				if (homing.getName().equals("water energy"))
					power = 362;
				break;
			case 4:
				if (homing.getName().equals("cyclone servant"))
					power = 1166;
				if (homing.getName().equals("fire energy"))
					power = 313;
				if (homing.getName().equals("wind servant"))
					power = 373;
				if (homing.getName().equals("stone energy"))
					power = 384;
				break;
			case 5:
				if (homing.getName().equals("cyclone servant"))
					power = 1221;
				break;
			case 6:
				if (homing.getName().equals("cyclone servant"))
					power = 1283;
				break;
			case 7:
				if (homing.getName().equals("cyclone servant"))
					power = 1342;
				break;
		}

		switch (homing.getLevel()) {
			case 65:
				if (homing.getName().equals("elemental energy"))
					power = 1100;
				break;
		}

		return getStat(StatEnum.MAGICAL_ATTACK, power, calculationTypes);
	}
}
