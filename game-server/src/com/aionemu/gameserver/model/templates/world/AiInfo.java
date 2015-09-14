package com.aionemu.gameserver.model.templates.world;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AiInfo")
public class AiInfo {

	public static final AiInfo DEFAULT = new AiInfo();

	@XmlAttribute(name = "chase_target")
	private int chaseTarget = 50;
	@XmlAttribute(name = "chase_home")
	private int chaseHome = 200;

	public final int getChaseTarget() {
		return chaseTarget;
	}

	public final int getChaseHome() {
		return chaseHome;
	}

}
