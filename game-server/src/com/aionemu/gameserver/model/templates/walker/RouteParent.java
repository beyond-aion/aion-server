package com.aionemu.gameserver.model.templates.walker;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RouteParent", propOrder = { "versions" })
public class RouteParent {

	@XmlElement(name = "version", required = true)
	protected List<RouteVersion> versions;

	@XmlAttribute(required = true)
	protected String id;

	public List<RouteVersion> getRouteVersion() {
		if (versions == null)
			versions = new FastTable<>();
		return this.versions;
	}

	public String getId() {
		return id;
	}

}
