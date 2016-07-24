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

	@XmlAttribute(name = "step", required = true)
	private int routeStep;

	@XmlAttribute(name = "x", required = true)
	private float locX;

	@XmlAttribute(name = "y", required = true)
	private float locY;

	@XmlAttribute(name = "z", required = true)
	private float locZ;

	@XmlAttribute(name = "rest_time", required = true)
	private int time = 0;

	@XmlAttribute(name = "stop")
	private boolean isStop = false;

	@XmlTransient
	private RouteStep nextStep;

	public RouteStep() {
	}

	public RouteStep(int step, float x, float y, float z, int restTime) {
		routeStep = step;
		locX = x;
		locY = y;
		locZ = z;
		time = restTime;
	}

	public float getX() {
		return locX;
	}

	public float getY() {
		return locY;
	}

	public float getZ() {
		return locZ;
	}

	public void setZ(float z) {
		locZ = z;
	}

	public int getRestTime() {
		return time;
	}

	public RouteStep getNextStep() {
		return nextStep;
	}

	public void setNextStep(RouteStep nextStep) {
		this.nextStep = nextStep;
	}

	public int getRouteStep() {
		return routeStep;
	}

	public boolean isStop() {
		return isStop;
	}

}
