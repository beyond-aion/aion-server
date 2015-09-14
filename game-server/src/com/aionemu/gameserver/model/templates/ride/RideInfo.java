package com.aionemu.gameserver.model.templates.ride;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.Bounds;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RideInfo", propOrder = { "bounds" })
public class RideInfo {

	protected Bounds bounds;

	@XmlAttribute(name = "cost_fp")
	protected Integer costFp;

	@XmlAttribute(name = "start_fp")
	protected int startFp;

	@XmlAttribute(name = "sprint_speed")
	protected float sprintSpeed;

	@XmlAttribute(name = "fly_speed")
	protected float flySpeed;

	@XmlAttribute(name = "move_speed")
	protected float moveSpeed;

	@XmlAttribute
	protected Integer type;

	@XmlAttribute(required = true)
	protected int id;

	/**
	 * Gets the value of the bounds property.
	 * 
	 * @return possible object is {@link Bounds }
	 */
	public Bounds getBounds() {
		return bounds;
	}

	public Integer getCostFp() {
		return costFp;
	}

	public int getStartFp() {
		return startFp;
	}

	public float getSprintSpeed() {
		return sprintSpeed;
	}

	public float getFlySpeed() {
		return flySpeed;
	}

	public float getMoveSpeed() {
		return moveSpeed;
	}

	public Integer getType() {
		return type;
	}

	public int getNpcId() {
		return id;
	}

	public boolean canSprint() {
		return sprintSpeed != 0;
	}
}
