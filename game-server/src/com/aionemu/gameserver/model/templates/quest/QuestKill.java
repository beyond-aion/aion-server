package com.aionemu.gameserver.model.templates.quest;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

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

	@XmlTransient
	private List<Integer> npcIdSet;

	/**
	 * @return the seq
	 */
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

	/**
	 * @return the npcIds
	 */
	public List<Integer> getNpcIds() {
		if (npcIdSet == null)
			npcIdSet = new ArrayList<>();
		if (npcIds != null) {
			npcIdSet.addAll(npcIds);
			npcIds.clear();
			npcIds = null;
		}
		return npcIdSet;
	}
}
