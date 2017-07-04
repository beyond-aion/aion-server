package com.aionemu.gameserver.model.templates.walker;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

/**
 * @author KKnD, Rolandas
 */
@XmlRootElement(name = "routestep")
@XmlAccessorType(XmlAccessType.FIELD)
public class RouteStep {

	@XmlAttribute(name = "x", required = true)
	private float x;

	@XmlAttribute(name = "y", required = true)
	private float y;

	@XmlAttribute(name = "z", required = true)
	private float z;

	@XmlAttribute(name = "rest_time", required = true)
	private int restTime = 0;

	@XmlTransient
	private int stepIndex;

	@XmlTransient
	private boolean isLastStep;

	protected RouteStep() {
	}

	public RouteStep(float x, float y, float z, int restTime) {
		this.x = x;
		this.y = y;
		this.z = z;
		this.restTime = restTime;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		this.z = z;
	}

	public int getRestTime() {
		return restTime;
	}

	public int getStepIndex() {
		return stepIndex;
	}

	public void setStepIndex(int stepIndex) {
		this.stepIndex = stepIndex;
	}

	public boolean isLastStep() {
		return isLastStep;
	}

	public void setIsLastStep(boolean isLastStep) {
		this.isLastStep = isLastStep;
	}
}
