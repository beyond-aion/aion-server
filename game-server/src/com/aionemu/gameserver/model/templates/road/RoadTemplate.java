package com.aionemu.gameserver.model.templates.road;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.utils3d.Point3D;

/**
 * @author SheppeR
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Road")
public class RoadTemplate {

	@XmlAttribute(name = "name")
	protected String name;

	@XmlAttribute(name = "map")
	protected int map;

	@XmlAttribute(name = "radius")
	protected float radius;

	@XmlElement(name = "center")
	protected RoadPoint center;

	@XmlElement(name = "p1")
	protected RoadPoint p1;

	@XmlElement(name = "p2")
	protected RoadPoint p2;

	@XmlElement(name = "roadexit")
	protected RoadExit roadExit;

	public String getName() {
		return name;
	}

	public int getMap() {
		return map;
	}

	public float getRadius() {
		return radius;
	}

	public RoadPoint getCenter() {
		return center;
	}

	public RoadPoint getP1() {
		return p1;
	}

	public RoadPoint getP2() {
		return p2;
	}

	public RoadExit getRoadExit() {
		return roadExit;
	}

	public RoadTemplate() {

	};

	public RoadTemplate(String name, int mapId, Point3D center, Point3D p1, Point3D p2) {
		this.name = name;
		this.map = mapId;
		this.radius = 6;
		this.center = new RoadPoint(center);
		this.p1 = new RoadPoint(p1);
		this.p2 = new RoadPoint(p2);
	}
}
