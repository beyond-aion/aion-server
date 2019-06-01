package com.aionemu.gameserver.model.templates.housing;

import java.util.Objects;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "address")
public class HouseAddress {

	@XmlTransient
	private HousingLand land;

	@XmlAttribute(name = "exit_z")
	private Float exitZ;

	@XmlAttribute(name = "exit_y")
	private Float exitY;

	@XmlAttribute(name = "exit_x")
	private Float exitX;

	@XmlAttribute(name = "exit_map")
	private Integer exitMap;

	@XmlAttribute(required = true)
	private float z;

	@XmlAttribute(required = true)
	private float y;

	@XmlAttribute(required = true)
	private float x;

	@XmlAttribute(name = "town", required = true)
	private int townId;

	@XmlAttribute(required = true)
	private int map;

	@XmlAttribute(required = true)
	private int id;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		this.land = Objects.requireNonNull((HousingLand) parent);
	}

	public HousingLand getLand() {
		return land;
	}

	public Float getExitZ() {
		return exitZ;
	}

	public Float getExitY() {
		return exitY;
	}

	public Float getExitX() {
		return exitX;
	}

	public Integer getExitMapId() {
		return exitMap;
	}

	public float getZ() {
		return z;
	}

	public float getY() {
		return y;
	}

	public float getX() {
		return x;
	}

	public int getMapId() {
		return map;
	}

	public int getId() {
		return id;
	}

	public int getTownId() {
		return townId;
	}
}
