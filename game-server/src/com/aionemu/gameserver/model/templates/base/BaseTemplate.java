package com.aionemu.gameserver.model.templates.base;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.base.BaseColorType;
import com.aionemu.gameserver.model.base.BaseOccupier;
import com.aionemu.gameserver.model.base.BaseType;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Base")
public class BaseTemplate {

	@XmlAttribute(name = "id")
	protected int id;
	@XmlAttribute(name = "world")
	protected int world;
	@XmlAttribute(name = "type")
	protected BaseType type;
	@XmlAttribute(name = "color")
	protected BaseColorType color;
	@XmlAttribute(name = "default_occupier")
	protected BaseOccupier defaultOccupier = BaseOccupier.BALAUR;


	public int getId() {
		return this.id;
	}

	public int getWorldId() {
		return this.world;
	}
	
	public BaseType getType() {
		return type;
	}

	public BaseColorType getColor() {
		return color;
	}

	public BaseOccupier getDefaultOccupier() {
		return defaultOccupier;
	}
}
