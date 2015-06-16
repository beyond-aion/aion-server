package com.aionemu.gameserver.model.templates.stats;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StatsSet", propOrder = { "modifiers" })
public class AbsoluteStatsTemplate {

	@XmlElement(required = true)
	protected ModifiersTemplate modifiers;

	@XmlAttribute(required = true)
	protected int id;

	public ModifiersTemplate getModifiers() {
		return this.modifiers;
	}

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}
}
