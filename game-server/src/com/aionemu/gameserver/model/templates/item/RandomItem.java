package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author vlog, Neon
 */
@XmlType(name = "RandomItem")
public class RandomItem {

	@XmlAttribute(name = "type")
	private RandomType type;
	@XmlAttribute(name = "min_count")
	private int minCount = 1;
	@XmlAttribute(name = "max_count")
	private int maxCount;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (minCount <= 0)
			throw new IllegalArgumentException("Decomposable random reward item of type " + type + " min_count (" + minCount + ") must be greater than 0");
		if (maxCount == 0)
			maxCount = minCount;
		else if (maxCount < minCount)
			throw new IllegalArgumentException(
				"Decomposable random reward item of type " + type + " max_count (" + maxCount + ") must be unset or greater than min_count (" + minCount + ")");
	}

	public RandomType getType() {
		return type;
	}

	public int getMinCount() {
		return minCount;
	}

	public int getMaxCount() {
		return maxCount;
	}
}
