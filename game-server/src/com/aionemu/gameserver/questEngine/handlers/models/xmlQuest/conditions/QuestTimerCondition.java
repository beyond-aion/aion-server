package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.conditions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.PlayerController;
import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * Checks whether a timer of this quest is running. Timers don't survive a relog, so a step waiting for one needs a way to notice it's gone.
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestTimerCondition")
public class QuestTimerCondition extends QuestCondition {

	@XmlAttribute(required = true)
	protected boolean running;

	@Override
	public boolean doCheck(QuestEnv env) {
		PlayerController controller = env.getPlayer().getController();
		boolean isRunning = controller.getQuestTimerQuestId() == env.getQuestId() || controller.hasInvisibleQuestTimer(env.getQuestId());
		return switch (getOp()) {
			case EQUAL -> isRunning == running;
			case NOT_EQUAL -> isRunning != running;
			default -> false;
		};
	}
}
