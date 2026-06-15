package com.aionemu.gameserver.model.templates.npcskill;

/**
 * @author Yeats
 */
public class QueuedNpcSkillTemplate extends NpcSkillTemplate {

	public QueuedNpcSkillTemplate(int id, int lv) {
		this(id, lv, -1);
	}

	public QueuedNpcSkillTemplate(int id, int lv, int nextSkillTime) {
		this(id, lv, nextSkillTime, NpcSkillTargetAttribute.MOST_HATED);
	}

	public QueuedNpcSkillTemplate(int id, int lv, int nextSkillTime, NpcSkillTargetAttribute npcSkillTargetAttribute) {
		this.id = id;
		this.lv = lv;
		this.prob = 100;
		this.nextSkillTime = nextSkillTime;
		this.target = npcSkillTargetAttribute;
	}
}
