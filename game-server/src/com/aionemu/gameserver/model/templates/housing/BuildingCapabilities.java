package com.aionemu.gameserver.model.templates.housing;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "caps")
public class BuildingCapabilities {

	@XmlAttribute(required = true)
	protected boolean addon;

	@XmlAttribute(required = true)
	protected int emblemId;

	@XmlAttribute(required = true)
	protected boolean floor;

	@XmlAttribute(required = true)
	protected boolean room;

	@XmlAttribute(required = true)
	protected int interior;

	@XmlAttribute(required = true)
	protected int exterior;

	public boolean canHaveAddon() {
		return addon;
	}

	public int getEmblemId() {
		return emblemId;
	}

	public boolean canChangeFloor() {
		return floor;
	}

	public boolean canChangeRoom() {
		return room;
	}

	public int canChangeInterior() {
		return interior;
	}

	public int canChangeExterior() {
		return exterior;
	}

}
