package com.aionemu.gameserver.model.templates.npcskill;

/**
 * @author Yeats
 */
public class QueuedNpcSkillTemplate extends NpcSkillTemplate {

	public QueuedNpcSkillTemplate(int id, int lv, int prob) {
		this(id, lv, prob, 0, 0);
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd, int nextSkillTime) {
		this(id, lv, prob, cd, nextSkillTime, NpcSkillTargetAttribute.MOST_HATED);
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd, int nextSkillTime, NpcSkillTargetAttribute npcSkillTargetAttribute) {
		this(id, lv, prob, cd, nextSkillTime, npcSkillTargetAttribute, 0, 0, 0, 0, ConjunctionType.AND, null);
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd, int nextSkillTime, NpcSkillTargetAttribute target, int minHp, int maxHp,
		int minTime, int maxTime, ConjunctionType conjunction, NpcSkillConditionTemplate conditionTemplate) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.cd = cd;
		this.nextSkillTime = nextSkillTime;
		this.target = target;
		this.minHp = minHp;
		this.maxHp = maxHp;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.conjunction = conjunction;
		this.conditionTemplate = conditionTemplate;
	}

}
