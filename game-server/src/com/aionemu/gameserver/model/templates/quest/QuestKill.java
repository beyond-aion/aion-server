package com.aionemu.gameserver.model.templates.quest;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "QuestKill")
public class QuestKill {

	@XmlAttribute(name = "seq")
	private int seq;

	@XmlAttribute(name = "npc_ids")
	private List<Integer> npcIds;

	@XmlAttribute(name = "count")
	private int kill;

	@XmlAttribute(name = "var")
	private int var;

	@XmlAttribute(name = "step")
	private int step;

	public int getSequenceNumber() {
		return seq;
	}

	public int getKillCount() {
		return kill;
	}

	public int getVar() {
		return var;
	}

	public int getQuestStep() {
		return step;
	}

	public List<Integer> getNpcIds() {
		return npcIds;
	}
}
