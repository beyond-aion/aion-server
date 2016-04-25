package com.aionemu.gameserver.model.templates.npcskill;

/**
 * @author Yeats 24.04.2016.
 */
public class QueuedNpcSkillTemplate extends NpcSkillTemplate {

	private boolean ignoreNextSkillTime = false;

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int minHp, int maxHp, int minTime, int maxTime, ConjunctionType conjunction, int cd, int nextSkillTime, boolean ignoreNextSkillTime, NpcSkillConditionTemplate conditionTemplate, NpcSkillTargetAttribute target) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.minHp = minHp;
		this.maxHp = maxHp;
		this.minTime = minTime;
		this.maxTime = maxTime;
		this.conjunction = conjunction;
		this.cd = cd;
		this.nextSkillTime = nextSkillTime;
		this.conditionTemplate = conditionTemplate;
		this.target = target;
		this.ignoreNextSkillTime = ignoreNextSkillTime;
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, boolean ignoreNextSkillTime) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.ignoreNextSkillTime = ignoreNextSkillTime;
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.cd = cd;
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd, boolean ignoreNextSkillTime) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.cd = cd;
		this.ignoreNextSkillTime = ignoreNextSkillTime;
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd, int nextSkillTime) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.cd = cd;
		this.nextSkillTime = nextSkillTime;
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd, int nextSkillTime, boolean ignoreNextSkillTime) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.cd = cd;
		this.nextSkillTime = nextSkillTime;
		this.ignoreNextSkillTime = ignoreNextSkillTime;
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd, int nextSkillTime, NpcSkillTargetAttribute target) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.cd = cd;
		this.nextSkillTime = nextSkillTime;
		this.target = target;
	}

	public QueuedNpcSkillTemplate(int id, int lv, int prob, int cd, int nextSkillTime, NpcSkillTargetAttribute target, boolean ignoreNextSkillTime) {
		this.id = id;
		this.lv = lv;
		this.prob = prob;
		this.cd = cd;
		this.nextSkillTime = nextSkillTime;
		this.target = target;
		this.ignoreNextSkillTime = ignoreNextSkillTime;
	}

	public boolean isIgnoreNextSkillTime() {
		return ignoreNextSkillTime;
	}
}
