package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.conditions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestVarCondition")
public class QuestVarCondition extends QuestCondition {

	@XmlAttribute(required = true)
	protected int value;
	@XmlAttribute(name = "var_id", required = true)
	protected int varId;

	@Override
	public boolean doCheck(QuestEnv env) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(env.getQuestId());
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVars().getVarById(varId);
		switch (getOp()) {
			case EQUAL:
				return var == value;
			case GREATER:
				return var > value;
			case GREATER_EQUAL:
				return var >= value;
			case LESSER:
				return var < value;
			case LESSER_EQUAL:
				return var <= value;
			case NOT_EQUAL:
				return var != value;
			default:
				return false;
		}
	}

}
