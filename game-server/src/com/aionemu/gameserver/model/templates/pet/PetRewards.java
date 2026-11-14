package com.aionemu.gameserver.model.templates.pet;

import java.util.List;

import jakarta.xml.bind.annotation.*;

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
		return results == null ? List.of() : results;
	}

	public FoodType getType() {
		return type;
	}

	public boolean isLoved() {
		return loved;
	}

}
