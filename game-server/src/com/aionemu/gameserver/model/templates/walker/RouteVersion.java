package com.aionemu.gameserver.model.templates.walker;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RouteVersion")
public class RouteVersion {

	@XmlAttribute(required = true)
	protected String id;

	public String getId() {
		return id;
	}

}
