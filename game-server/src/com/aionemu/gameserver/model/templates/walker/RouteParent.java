package com.aionemu.gameserver.model.templates.walker;

import java.util.List;

import jakarta.xml.bind.annotation.*;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RouteParent", propOrder = { "versions" })
public class RouteParent {

	@XmlElement(name = "version", required = true)
	protected List<RouteVersion> versions;

	@XmlAttribute(required = true)
	protected String id;

	public List<RouteVersion> getRouteVersion() {
		return versions == null ? List.of() : versions;
	}

	public String getId() {
		return id;
	}

}
