package com.aionemu.gameserver.model.templates.spawns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "HouseSpawn")
public class HouseSpawn {

	@XmlAttribute(name = "x", required = true)
	protected float x;
	
	@XmlAttribute(name = "y", required = true)
	protected float y;
	
	@XmlAttribute(name = "z", required = true)
	protected float z;

	@XmlAttribute(name = "h")
	protected Byte h;
	
	@XmlAttribute(name = "static_id")
	private int staticId;
	
	@XmlAttribute(name = "type", required = true)
	protected SpawnType type;
	
	public float getX() {
		return x;
	}

	public void setX(float value) {
		this.x = value;
	}

	public float getY() {
		return y;
	}

	public void setY(float value) {
		this.y = value;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float value) {
		this.z = value;
	}

	public byte getH() {
		if (h == null)
			return ((byte) 0);

		return h;
	}

	public void setH(Byte value) {
		this.h = value;
	}

	public SpawnType getType() {
		return type;
	}

	public void setType(SpawnType value) {
		this.type = value;
	}

	public int getStaticId() {
		return staticId;
	}

	public void setStaticId(int staticId) {
		this.staticId = staticId;
	}

}
