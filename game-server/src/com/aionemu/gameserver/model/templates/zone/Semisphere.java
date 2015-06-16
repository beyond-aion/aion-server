package com.aionemu.gameserver.model.templates.zone;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Semisphere")
public class Semisphere extends Sphere {

	public Semisphere() {
		super();
	}

	public Semisphere(float x, float y, float z, float radius) {
		super(x, y, z, radius);
	}

}
