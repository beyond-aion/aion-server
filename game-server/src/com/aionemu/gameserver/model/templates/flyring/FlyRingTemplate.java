package com.aionemu.gameserver.model.templates.flyring;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.geometry.Point3D;

/**
 * @author M@xx
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FlyRing")
public class FlyRingTemplate {

	@XmlAttribute(name = "name")
	protected String name;

	@XmlAttribute(name = "map")
	protected int map;

	@XmlAttribute(name = "radius")
	protected float radius;

	@XmlElement(name = "center")
	protected FlyRingPoint center;

	@XmlElement(name = "p1")
	protected FlyRingPoint p1;

	@XmlElement(name = "p2")
	protected FlyRingPoint p2;

	public String getName() {
		return name;
	}

	public int getMap() {
		return map;
	}

	public float getRadius() {
		return radius;
	}

	public FlyRingPoint getCenter() {
		return center;
	}

	public FlyRingPoint getP1() {
		return p1;
	}

	public FlyRingPoint getP2() {
		return p2;
	}

	public FlyRingTemplate() {
	};

	public FlyRingTemplate(String name, int mapId, Point3D center, Point3D p1, Point3D p2, int radius) {
		this.name = name;
		this.map = mapId;
		this.radius = radius;
		this.center = new FlyRingPoint(center);
		this.p1 = new FlyRingPoint(p1);
		this.p2 = new FlyRingPoint(p2);
	}

}
