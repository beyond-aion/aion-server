package com.aionemu.gameserver.model.templates.npcskill;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author AionChs Master, nrg, Yeats
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "npc_skill")
public class NpcSkillTemplate {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "lv")
	protected int lv;
	@XmlAttribute(name = "prob")
	protected int prob;
	@XmlAttribute(name = "min_hp")
	protected int minHp = 0;
	@XmlAttribute(name = "max_hp")
	protected int maxHp = 100;
	@XmlAttribute(name = "max_time")
	protected int maxTime = 0;
	@XmlAttribute(name = "min_time")
	protected int minTime = 0;
	@XmlAttribute(name = "conjunction")
	protected ConjunctionType conjunction = ConjunctionType.AND;
	@XmlAttribute(name = "cd")
	protected int cd = 0;
	@XmlAttribute(name = "is_post_spawn")
	protected boolean is_post_spawn = false;
	@XmlAttribute(name = "prio")
	protected int prio = 0;
	@XmlAttribute(name = "next_skill_time")
	protected int nextSkillTime = -1; // -1 = random time between 3s and 9s
	@XmlElement(name = "cond")
	protected NpcSkillConditionTemplate conditionTemplate;
	@XmlElement(name = "spawn_npc")
	protected NpcSkillSpawn spawn;
	@XmlAttribute(name = "next_chain_id")
	protected int nextChainId = 0;
	@XmlAttribute(name = "chain_id")
	protected int chainId = 0;
	@XmlAttribute(name = "max_chain_time")
	protected int maxChainTime = 15000;
	@XmlAttribute(name = "target")
	protected NpcSkillTargetAttribute target = NpcSkillTargetAttribute.MOST_HATED;

	public int getSkillId() {
		return id;
	}

	public int getSkillLevel() {
		return lv;
	}

	public int getProbability() {
		return prob;
	}

	public int getMinhp() {
		return minHp;
	}

	public int getMaxhp() {
		return maxHp;
	}

	public int getMinTime() {
		return minTime;
	}

	public int getMaxTime() {
		return maxTime;
	}

	/**
	 * Gets the value of the conjunction property.
	 * 
	 * @return possible object is {@link ConjunctionType }
	 */
	public ConjunctionType getConjunctionType() {
		return conjunction;
	}

	public int getCooldown() {
		return cd;
	}

	public boolean isPostSpawn() {
		return is_post_spawn;
	}

	public int getPriority() {
		return prio;
	}

	public NpcSkillConditionTemplate getConditionTemplate() {
		return conditionTemplate;
	}

	public NpcSkillSpawn getSpawn() {
		return spawn;
	}

	public SkillTemplate getSkillTemplate() {
		if (id <= 0) {
			return null;
		}
		return DataManager.SKILL_DATA.getSkillTemplate(id);
	}

	public int getNextSkillTime() {
		return nextSkillTime;
	}

	public int getNextChainId() {
		return nextChainId;
	}

	public int getChainId() {
		return chainId;
	}

	public int getMaxChainTime() {
		return maxChainTime;
	}

	public NpcSkillTargetAttribute getTarget() {
		return target;
	}
}
