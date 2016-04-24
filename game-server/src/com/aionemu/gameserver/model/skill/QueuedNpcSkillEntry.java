package com.aionemu.gameserver.model.skill;

import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;

/**
 * @author Yeats 24.04.2016.
 */
public class QueuedNpcSkillEntry extends NpcSkillTemplateEntry {

	public QueuedNpcSkillEntry(QueuedNpcSkillTemplate template) {
		super(template);
	}

	@Override
	public boolean isQueued() {
		return true;
	}
}
