package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.ChainSkill;
import com.aionemu.gameserver.skillengine.model.ChainSkills;
import com.aionemu.gameserver.skillengine.model.Skill;

/**
 * @author ATracer, kecimis, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChainCondition")
public class ChainCondition extends Condition {

	@XmlAttribute(name = "selfcount")
	private int selfCount = 1;
	@XmlAttribute(name = "precount")
	private int preCount = 1;
	@XmlAttribute(name = "category")
	private String category;
	@XmlAttribute(name = "precategory")
	private String preCategory;
	@XmlAttribute(name = "time")
	private int time;

	@Override
	public boolean validate(Skill env) {
		if (env.getEffector() instanceof Player player) {
			ChainSkills chain =  player.getChainSkills();
			ChainSkill currentSkill = chain.getCurrentChainSkill();

			if (shouldReset(chain, env))
				chain.resetChain();

			if (preCategory != null) {
				if (currentSkill.getCategory().equals(preCategory)) {
					if (currentSkill.getUseCount() < preCount) // preCategory skill must have been activated x times
						return false;
				} else if (!chain.getPreviousChainSkill().getCategory().equals(preCategory)) { // previously activated skill must match
					return false;
				}
			}
		}

		env.setChainCategory(category);
		env.setChainUsageDuration(time);
		return true;
	}

	private boolean shouldReset(ChainSkills chain, Skill env) {
		ChainSkill currentSkill = chain.getCurrentChainSkill();
		if (!currentSkill.getCategory().isEmpty()) {
			if (chain.isChainExpired()) // check max allowed use time
				return true;

			if (preCategory == null && category.contains("_1TH")) { // first skill of a chain
				if (!currentSkill.getCategory().equals(category)) // other skill
					return true;
				if (currentSkill.getUseCount() == selfCount) // same skill
					return true;
				int maxActiveDuration = time > 0 ? time : env.getCooldown() * 100; // template cooldown is seconds * 10...
				if (System.currentTimeMillis() > currentSkill.getLastUseTime() + maxActiveDuration)
					return true;
			}
		}

		return false;
	}

	/**
	 * @return Number of allowed skill activations of this chain skill.
	 */
	public int getAllowedActivations() {
		return selfCount;
	}

	/**
	 * @return the category
	 */
	public String getCategory() {
		return category;
	}

	/**
	 * @return the time
	 */
	public int getTime() {
		return time;
	}
}
