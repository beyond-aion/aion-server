package com.aionemu.gameserver.model.templates.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 *
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Base")
public class BaseTemplate {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "world")
	protected int world;

	/**
	 * @return the location id
	 */
	public int getId() {
		return this.id;
	}

	/**
	 * @return the world id
	 */
	public int getWorldId() {
		return this.world;
	}

}