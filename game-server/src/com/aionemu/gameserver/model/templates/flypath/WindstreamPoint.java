package com.aionemu.gameserver.model.templates.flypath;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;

@XmlAccessorType(XmlAccessType.NONE)
public class WindstreamPoint {

	@XmlAttribute(name = "x", required = true)
	private float x;
	@XmlAttribute(name = "y", required = true)
	private float y;
	@XmlAttribute(name = "z", required = true)
	private float z;
	@XmlAttribute(name = "entry")
	private boolean entry = true;
	@XmlAttribute(name = "exit")
	private boolean exit = true;

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public boolean canEnter() {
		return entry;
	}

	public boolean canExit() {
		return exit;
	}
}
