package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
