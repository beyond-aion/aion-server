package com.aionemu.gameserver.model.templates.ai;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Ai")
public class AITemplate {

	@XmlElement(name = "summons")
	private Summons summons;

	@XmlElement(name = "bombs")
	private Bombs bombs;

	@XmlAttribute(name = "npcId")
	private int npcId;

	public Summons getSummons() {
		return summons;
	}

	public Bombs getBombs() {
		return bombs;
	}

	public int getNpcId() {
		return npcId;
	}
}
