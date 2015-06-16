package com.aionemu.gameserver.model.templates.pet;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetFeedResult")
public class PetFeedResult {

	@XmlAttribute(required = true)
	protected int item;

	@XmlAttribute
	protected String name;

	public int getItem() {
		return item;
	}

	@Override
	public String toString() {
		return name + " (" + item + ")";
	}

}
