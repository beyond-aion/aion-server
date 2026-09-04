package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.operations;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.QuestService;

/**
 * Starts a quest timer which fires {@code on_timer_end_event} when it expires. Visible timers also show the remaining time to the player.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AddTimerOperation")
public class AddTimerOperation extends QuestOperation {

	@XmlAttribute(required = true)
	protected int seconds;
	/** whether the player sees the remaining time */
	@XmlAttribute
	protected boolean visible;

	@Override
	public void doOperate(QuestEnv env) {
		if (visible)
			QuestService.questTimerStart(env, seconds);
		else
			QuestService.invisibleTimerStart(env, seconds);
	}
}
