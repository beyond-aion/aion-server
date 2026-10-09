package com.aionemu.gameserver.model.templates.walker;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RouteVersion")
public class RouteVersion {

	@XmlAttribute(required = true)
	protected String id;

	public String getId() {
		return id;
	}

}
