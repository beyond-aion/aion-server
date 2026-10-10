package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropRatings")
public class GlobalDropRatings {

	@XmlElement(name = "gd_rating")
	protected List<GlobalDropRating> gdRatings;

	public List<GlobalDropRating> getGlobalDropRatings() {
		if (gdRatings == null) {
			gdRatings = new ArrayList<>();
		}
		return this.gdRatings;
	}

}
