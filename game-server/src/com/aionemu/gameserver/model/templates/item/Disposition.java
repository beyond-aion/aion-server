package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Disposition")
public class Disposition {

	@XmlAttribute
	protected int count;

	@XmlAttribute
	protected int id;

	public int getCount() {
		return count;
	}

	public int getId() {
		return id;
	}

}
