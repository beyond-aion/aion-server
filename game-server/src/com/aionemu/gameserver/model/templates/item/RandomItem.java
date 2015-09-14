package com.aionemu.gameserver.model.templates.item;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;

/**
 * @author vlog
 */
@XmlType(name = "RandomItem")
public class RandomItem {

	@XmlAttribute(name = "type")
	protected RandomType type;
	@XmlAttribute(name = "count")
	protected int count;
	@XmlAttribute(name = "rnd_min")
	public int rndMin;
	@XmlAttribute(name = "rnd_max")
	public int rndMax;

	public int getCount() {
		return count;
	}

	public RandomType getType() {
		return type;
	}

	public int getRndMin() {
		return rndMin;
	}

	public int getRndMax() {
		return rndMax;
	}

	public final int getResultCount() {
		if (count == 0 && rndMin == 0 && rndMax == 0) {
			return 1;
		} else if (rndMin > 0 || rndMax > 0) {
			if (rndMax < rndMin) {
				LoggerFactory.getLogger(RandomItem.class).warn("Wrong rnd result item definition {} {}", rndMin, rndMax);
				return 1;
			} else {
				return Rnd.get(rndMin, rndMax);
			}
		} else {
			return count;
		}
	}
}
