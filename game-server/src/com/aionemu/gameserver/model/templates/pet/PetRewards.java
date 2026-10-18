package com.aionemu.gameserver.model.templates.pet;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author Rolandas
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PetRewards", propOrder = { "results" })
public class PetRewards {

	@XmlElement(name = "result")
	protected List<PetFeedResult> results;

	@XmlAttribute(name = "group", required = true)
	protected FoodType type;

	@XmlAttribute
	protected boolean loved = false;

	public List<PetFeedResult> getResults() {
		if (results == null) {
			results = new ArrayList<>();
		}
		return this.results;
	}

	public FoodType getType() {
		return type;
	}

	public boolean isLoved() {
		return loved;
	}

}
