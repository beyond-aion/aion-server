package com.aionemu.gameserver.model.templates.item;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Idian")
public class Idian {

	@XmlAttribute(name = "burn_defend")
	private int burnDefend;

	@XmlAttribute(name = "burn_attack")
	private int burnAttack;

	public int getBurnAttack() {
		return burnAttack;
	}

	public int getBurnDefend() {
		return burnDefend;
	}
}
