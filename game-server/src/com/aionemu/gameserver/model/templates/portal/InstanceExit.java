package com.aionemu.gameserver.model.templates.portal;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "InstanceExit")
public class InstanceExit {

	@XmlAttribute(name = "instance_id")
	protected int instanceId;
	@XmlAttribute(name = "exit_world")
	protected int exitWorld;
	@XmlAttribute(name = "race")
	protected Race race = Race.PC_ALL;
	@XmlAttribute(name = "x")
	protected float x;
	@XmlAttribute(name = "y")
	protected float y;
	@XmlAttribute(name = "z")
	protected float z;
	@XmlAttribute(name = "h")
	protected byte h;

	public Integer getInstanceId() {
		return instanceId;
	}

	public void setInstanceId(int value) {
		this.instanceId = value;
	}

	public int getExitWorld() {
		return exitWorld;
	}

	public void setExitWorld(int value) {
		this.exitWorld = value;
	}

	public Race getRace() {
		return race;
	}

	public void setRace(Race value) {
		this.race = value;
	}

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
		return h;
	}

	public void setH(byte value) {
		this.h = value;
	}

}
