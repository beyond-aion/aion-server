package com.aionemu.gameserver.model.templates.windstreams;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author LokiReborn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StreamLocations")
public class StreamLocations {

	@XmlElement(required = true)
	protected List<Location2D> location;

	public List<Location2D> getLocation() {
		if (location == null)
			location = new ArrayList<>();

		return this.location;
	}
}
