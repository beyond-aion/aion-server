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
 * @author ATracer
 * @modified kecimis
 * @reworked Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ChainCondition")
public class ChainCondition extends Condition {

	@XmlAttribute(name = "selfcount")
	private int selfCount;
	@XmlAttribute(name = "precount")
	private int preCount;
	@XmlAttribute(name = "category")
	private String category;
	@XmlAttribute(name = "precategory")
	private String preCategory;
	@XmlAttribute(name = "time")
	private int time;

	@Override
	public boolean validate(Skill env) {
		if (env.getEffector() instanceof Player) {
			long expireTime;
			long now = System.currentTimeMillis();
			ChainSkills chain = ((Player) env.getEffector()).getChainSkills();
			ChainSkill currentSkill = chain.getCurrentChainSkill();
			if (preCategory == null && category.contains("_1TH")) { // first skill of a chain
				if (currentSkill.getLastUseTime() > 0
					&& (now >= currentSkill.getLastUseTime() + env.getCooldown() || !currentSkill.getCategory().equals(category))) {
					chain.resetChain(); // resets previous and current skill
					currentSkill = chain.getCurrentChainSkill();
				}
			}

			if (currentSkill.getCategory().equals(category)) { // multicast
				if (currentSkill.getUseCount() >= selfCount) // max activation count
					return false;
				expireTime = currentSkill.getExpireTime();
			} else {
				expireTime = currentSkill.getLastUseTime() == 0 || time == 0 ? 0 : currentSkill.getLastUseTime() + time;
			}
			if (expireTime != 0 && now > expireTime) // check max allowed use time
				return false;

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
		env.setIsMultiCast(selfCount > 1);
		return true;
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
