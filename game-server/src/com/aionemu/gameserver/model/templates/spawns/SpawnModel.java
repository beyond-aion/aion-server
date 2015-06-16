package com.aionemu.gameserver.model.templates.spawns;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.TribeClass;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SpawnModel")
public class SpawnModel {

	@XmlAttribute(name = "tribe")
	private TribeClass tribe;

	@XmlAttribute(name = "ai")
	private String ai;

	/**
	 * @return the tribe
	 */
	public TribeClass getTribe() {
		return tribe;
	}

	/**
	 * @return the ai
	 */
	public String getAi() {
		return ai;
	}

}
