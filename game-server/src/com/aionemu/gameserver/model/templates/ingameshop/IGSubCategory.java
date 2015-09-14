package com.aionemu.gameserver.model.templates.ingameshop;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "IGSubCategory")
public class IGSubCategory {

	@XmlAttribute(required = true)
	protected int id;
	@XmlAttribute(required = true)
	protected String name;

	public int getId() {
		return id;
	}

	public String getName() {
		return name;
	}

}
