package com.aionemu.gameserver.model.templates.zone;

import java.util.List;

import javax.xml.bind.annotation.*;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.NONE)
@XmlType(name = "Zone")
public class ZoneTemplate {

	@XmlElement
	protected Points points;

	@XmlElement
	protected Cylinder cylinder;

	@XmlElement
	protected Sphere sphere;

	@XmlElement
	protected Semisphere semisphere;

	@XmlAttribute
	protected int flags = -1;

	@XmlAttribute
	protected int priority;

	@XmlAttribute
	private String name;

	protected void setXmlName(String name) {
		this.name = name;
	}

	@XmlAttribute
	protected int mapid;

	@XmlAttribute(name = "siege_id")
	protected List<Integer> siegeId;

	@XmlAttribute(name = "town_id")
	private int townId;

	@XmlAttribute(name = "area_type")
	protected AreaType areaType = AreaType.POLYGON;

	@XmlAttribute(name = "zone_type")
	protected ZoneClassName zoneType = ZoneClassName.SUB;

	public Points getPoints() {
		return points;
	}

	public Cylinder getCylinder() {
		return cylinder;
	}

	public Sphere getSphere() {
		return sphere;
	}

	public Semisphere getSemisphere() {
		return semisphere;
	}

	public int getPriority() {
		return priority;
	}

	public String getName() {
		return name;
	}

	public int getMapid() {
		return mapid;
	}

	public AreaType getAreaType() {
		return areaType;
	}

	public ZoneClassName getZoneType() {
		return zoneType;
	}

	public List<Integer> getSiegeId() {
		return siegeId;
	}

	public int getFlags() {
		return flags;
	}

	public int getTownId() {
		return townId;
	}

}
