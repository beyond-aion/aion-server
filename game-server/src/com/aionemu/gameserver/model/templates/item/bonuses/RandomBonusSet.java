package com.aionemu.gameserver.model.templates.item.bonuses;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RandomBonusSet")
public class RandomBonusSet {

	@XmlElement(required = true)
	private List<ModifiersTemplate> modifiers;

	@XmlAttribute(required = true)
	private int id;

	@XmlAttribute(name = "type", required = true)
	private StatBonusType bonusType;

	public List<ModifiersTemplate> getModifiers() {
		return modifiers;
	}

	public int getId() {
		return id;
	}

	public StatBonusType getBonusType() {
		return bonusType;
	}

}
