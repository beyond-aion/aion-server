package com.aionemu.gameserver.model.templates.road;

import javax.xml.bind.annotation.*;

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
}
