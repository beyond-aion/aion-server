package com.aionemu.gameserver.model.templates.ingameshop;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

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
