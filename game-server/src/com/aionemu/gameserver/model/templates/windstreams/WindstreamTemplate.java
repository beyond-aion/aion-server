package com.aionemu.gameserver.model.templates.windstreams;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author LokiReborn
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WindFlight")
public class WindstreamTemplate {

	@XmlElement(required = true)
	protected StreamLocations locations;
	@XmlAttribute
	protected int mapid;

	/**
	 * Gets the value of the locations property.
	 */
	public StreamLocations getLocations() {
		return locations;
	}

	/**
	 * Gets the value of the mapid property.
	 */
	public int getMapId() {
		return mapid;
	}
}
