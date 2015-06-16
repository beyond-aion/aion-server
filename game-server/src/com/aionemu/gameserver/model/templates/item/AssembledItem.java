package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AssembledItem")
public class AssembledItem {

	@XmlAttribute(name = "id", required = true)
	private int id;

	public int getId() {
		return id;
	}
}
