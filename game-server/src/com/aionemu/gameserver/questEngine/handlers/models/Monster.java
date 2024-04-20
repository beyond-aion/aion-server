package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author MrPoke, vlog, Bobobear, Artur, Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Monster")
public class Monster {

	@XmlAttribute(name = "var")
	protected int var;

	@XmlAttribute(name = "start_var")
	protected int startVar;

	@XmlAttribute(name = "end_var")
	protected int endVar;

	@XmlAttribute(name = "npc_ids")
	protected List<Integer> npcIds;

	@XmlAttribute(name = "npc_seq")
	private int npcSequence;

	@XmlAttribute(name = "step")
	private int step;

	@XmlAttribute(name = "spawner_object_id")
	protected int spawnerObjectId;

	public int getVar() {
		return var;
	}

	public void setVar(int value) {
		this.var = value;
	}

	public int getStartVar() {
		return startVar;
	}

	public void setStartVar(int value) {
		this.startVar = value;
	}

	public int getEndVar() {
		return endVar;
	}

	public void setEndVar(int value) {
		this.endVar = value;
	}

	public List<Integer> getNpcIds() {
		return npcIds;
	}

	public void addNpcIds(List<Integer> value) {
		if (this.npcIds == null)
			this.npcIds = new ArrayList<>();
		for (Integer npc : value) {
			if (!this.npcIds.contains(npc))
				this.npcIds.add(npc);
		}
	}

	public int getNpcSequence() {
		return npcSequence;
	}

	public void setNpcSequence(int value) {
		this.npcSequence = value;
	}

	public int getStep() {
		return step;
	}

	public void setStep(int value) {
		this.step = value;
	}

	public int getSpawnerNpcId() {
		return spawnerObjectId;
	}
}
