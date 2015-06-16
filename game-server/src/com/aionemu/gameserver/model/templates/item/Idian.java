package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
