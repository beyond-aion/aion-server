package com.aionemu.gameserver.model.templates.item.bonuses;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

import javolution.util.FastTable;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RandomBonus", propOrder = { "modifiers" })
public class RandomBonus {

	@XmlElement(required = true)
	protected List<ModifiersTemplate> modifiers;

	@XmlAttribute(required = true)
	protected int id;

	@XmlAttribute(name = "type", required = true)
	private StatBonusType bonusType;

	public List<ModifiersTemplate> getModifiers() {
		if (modifiers == null) {
			modifiers = new FastTable<>();
		}
		return this.modifiers;
	}

	/**
	 * Gets the value of the id property.
	 */
	public int getId() {
		return id;
	}

	public StatBonusType getBonusType() {
		return bonusType;
	}

}
