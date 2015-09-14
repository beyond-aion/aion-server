package com.aionemu.gameserver.model.templates.staticdoor;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author xTz
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "World")
public class StaticDoorWorld {

	@XmlAttribute(name = "world")
	protected int world;
	@XmlElement(name = "staticdoor")
	protected List<StaticDoorTemplate> staticDoorTemplate;

	/**
	 * @return the world
	 */
	public int getWorld() {
		return world;
	}

	/**
	 * @return the List<StaticDoorTemplate>
	 */
	public List<StaticDoorTemplate> getStaticDoors() {
		return staticDoorTemplate;
	}
}
