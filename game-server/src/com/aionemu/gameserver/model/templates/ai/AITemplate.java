package com.aionemu.gameserver.model.templates.ai;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
