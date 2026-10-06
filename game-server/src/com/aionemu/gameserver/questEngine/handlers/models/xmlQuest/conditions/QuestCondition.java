package com.aionemu.gameserver.questEngine.handlers.models.xmlQuest.conditions;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlSeeAlso;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.questEngine.model.ConditionOperation;
import com.aionemu.gameserver.questEngine.model.QuestEnv;

/**
 * @author Mr. Poke
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestCondition")
@XmlSeeAlso({ NpcIdCondition.class, DialogIdCondition.class, PcInventoryCondition.class, QuestVarCondition.class, QuestStatusCondition.class })
public abstract class QuestCondition {

	@XmlAttribute(required = true)
	protected ConditionOperation op;

	/**
	 * Gets the value of the op property.
	 * 
	 * @return possible object is {@link ConditionOperation }
	 */
	public ConditionOperation getOp() {
		return op;
	}

	public abstract boolean doCheck(QuestEnv env);

}
