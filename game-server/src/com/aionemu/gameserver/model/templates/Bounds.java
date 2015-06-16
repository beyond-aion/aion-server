package com.aionemu.gameserver.model.templates;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Bounds")
public class Bounds extends BoundRadius {

	public Bounds() {
	}

	public Bounds(float front, float side, float upper, float altitude) {
		super(front, side, upper);
		this.altitude = altitude;
	}

	@XmlAttribute
	private Float altitude;

	public Float getAltitude() {
		return altitude;
	}

}
