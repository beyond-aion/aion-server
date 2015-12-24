package com.aionemu.gameserver.model.templates.npcskill;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author AionChs Master, nrg
 * @reworked Yeats
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "npcskill")
public class NpcSkillTemplate {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "skillid")
	protected int skillid;
	@XmlAttribute(name = "skilllevel")
	protected int skilllevel;
	@XmlAttribute(name = "probability")
	protected int probability;
	@XmlAttribute(name = "minhp")
	protected int minhp = 0;
	@XmlAttribute(name = "maxhp")
	protected int maxhp = 0;
	@XmlAttribute(name = "maxtime")
	protected int maxtime = 0;
	@XmlAttribute(name = "mintime")
	protected int mintime = 0;
	@XmlAttribute(name = "conjunction")
	protected ConjunctionType conjunction = ConjunctionType.AND;
	@XmlAttribute(name = "cooldown")
	protected int cooldown = 0;
	@XmlAttribute(name = "useinspawned")
	protected boolean useinspawned = false;
	@XmlAttribute(name = "priority")
	protected int priority = 0;
	@XmlAttribute(name = "nextSkillTime")
	protected int nextSkillTime = -1; //-1 = random time between 3s and 9s
	@XmlElement(name = "cond")
	protected NpcSkillConditionTemplate conditionTemplate = null;
	@XmlAttribute(name = "nextChainId")
	protected int nextChainId = 0;
	@XmlAttribute(name = "chainId")
	protected int chainId = 0;
	@XmlAttribute(name = "maxChainTime")
	protected int maxChainTime = 15000;
	@XmlAttribute(name = "target")
	protected NpcSkillTargetAttribute target = NpcSkillTargetAttribute.MOST_HATED;

	/**
	 * @return the id
	 */
	public int getId() {
		return id;
	}

	/**
	 * @return the skillid
	 */
	public int getSkillid() {
		return skillid;
	}

	/**
	 * @return the skilllevel
	 */
	public int getSkillLevel() {
		return skilllevel;
	}

	/**
	 * @return the probability
	 */
	public int getProbability() {
		return probability;
	}

	/**
	 * @return the minhp
	 */
	public int getMinhp() {
		return minhp;
	}

	/**
	 * @return the maxhp
	 */
	public int getMaxhp() {
		return maxhp;
	}

	/**
	 * @return the mintime
	 */
	public int getMinTime() {
		return mintime;
	}

	/**
	 * @return the maxtime
	 */
	public int getMaxTime() {
		return maxtime;
	}

	/**
	 * Gets the value of the conjunction property.
	 * 
	 * @return possible object is {@link ConjunctionType }
	 */
	public ConjunctionType getConjunctionType() {
		return conjunction;
	}

	/**
	 * @return the cooldown
	 */
	public int getCooldown() {
		return cooldown;
	}

	/**
	 * @return the useinspawned
	 */
	public boolean getUseInSpawned() {
		return useinspawned;
	}
	
	/**
	 * @return priority
	 */
	public int getPriority() {
		return priority;
	}

	/**
	 * @return the conditionTemplate
	 */
	public NpcSkillConditionTemplate getConditionTemplate() {
		return conditionTemplate;
	}
	
	/**
	 * @return SkillTemplate
	 */
	public SkillTemplate getSkillTemplate() {
		if (skillid <= 0) {
			return null;
		}
		return DataManager.SKILL_DATA.getSkillTemplate(skillid);
	}

	/**
	 * @return nextSkillTime
	 */
	public int getNextSkillTime() {
		return nextSkillTime;
	}
	
	/**
	 * 
	 * @return nextChainId
	 */
	public int getNextChainId() {
		return nextChainId;
	}
	
	/**
	 * 
	 * @return chainId
	 */
	public int getChainId() {
		return chainId;
	}

	/**
	 * @return maxChainTime
	 */
	public int getMaxChainTime() {
		return maxChainTime;
	}

	/**
	 * @return target to select
	 */
	public NpcSkillTargetAttribute getTarget() {
		return target;
	}
}
