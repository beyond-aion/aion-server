package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;

/**
 * @author vlog
 * @modified Neon
 */
@XmlType(name = "RandomItem")
public class RandomItem {

	@XmlAttribute(name = "type")
	protected RandomType type;
	@XmlAttribute(name = "min_count")
	public int minCount = 1;
	@XmlAttribute(name = "max_count")
	public int maxCount;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		if (maxCount > 0 && maxCount < minCount)
			LoggerFactory.getLogger(ResultedItem.class).warn("Wrong count for decomposable random item:{}, min:{} max:{}", type, minCount, maxCount);
	}

	public RandomType getType() {
		return type;
	}

	public int getRndMin() {
		return minCount;
	}

	public int getRndMax() {
		return maxCount;
	}

	public final int getResultCount() {
		return maxCount == 0 ? minCount : Rnd.get(minCount, maxCount);
	}
}
