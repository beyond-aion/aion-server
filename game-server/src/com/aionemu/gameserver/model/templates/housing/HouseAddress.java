package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "address")
public class HouseAddress {

	@XmlAttribute(name = "exit_z")
	protected Float exitZ;

	@XmlAttribute(name = "exit_y")
	protected Float exitY;

	@XmlAttribute(name = "exit_x")
	protected Float exitX;

	@XmlAttribute(name = "exit_map")
	protected Integer exitMap;

	@XmlAttribute(required = true)
	protected float z;

	@XmlAttribute(required = true)
	protected float y;

	@XmlAttribute(required = true)
	protected float x;

	@XmlAttribute(name = "town", required = true)
	private int townId;

	@XmlAttribute(required = true)
	protected int map;

	@XmlAttribute(required = true)
	protected int id;

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

	public byte getTeleportHeading() {
		if (getMapId() == WorldMapType.HOUSING_IDDF_PERSONAL.getId()) // pernon apartment
			return 36;
		else if (getMapId() == WorldMapType.HOUSING_IDLF_PERSONAL.getId()) // oriel apartment
			return 63;
		return 0;
	}
}
