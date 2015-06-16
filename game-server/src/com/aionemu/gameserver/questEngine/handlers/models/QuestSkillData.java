package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author vlog, modified Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestSkillData")
public class QuestSkillData {

	@XmlAttribute(name = "ids", required = true)
	protected List<Integer> skillIds;
	@XmlAttribute(name = "start_var")
	protected int startVar = 0;
	@XmlAttribute(name = "end_var", required = true)
	protected int endVar;
	@XmlAttribute(name = "var_num")
	protected int varNum = 0;

	public List<Integer> getSkillIds() {
		return skillIds;
	}

	public int getVarNum() {
		return varNum;
	}

	public int getStartVar() {
		return startVar;
	}

	public int getEndVar() {
		return endVar;
	}
}
