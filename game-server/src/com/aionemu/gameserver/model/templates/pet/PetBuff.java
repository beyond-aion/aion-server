package com.aionemu.gameserver.model.templates.pet;

import java.util.List;

import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.stats.ModifiersTemplate;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetBuff", propOrder = { "modifiers" })
public class PetBuff {

	@XmlAttribute(required = true)
	protected int id;

	@XmlAttribute(name = "feed_count", required = true)
	protected int feedCount;

	@XmlElement(required = true)
	protected List<ModifiersTemplate> modifiers;

	public int getId() {
		return id;
	}

	public int getFeedCount() {
		return feedCount;
	}

	public List<ModifiersTemplate> getModifiers() {
		return modifiers;
	}
}
