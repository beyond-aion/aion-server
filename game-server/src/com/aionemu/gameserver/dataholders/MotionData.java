package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.skillengine.model.MotionTime;

import gnu.trove.map.hash.THashMap;

/**
 * @author kecimis
 */
@XmlRootElement(name = "motion_times")
@XmlAccessorType(XmlAccessType.FIELD)
public class MotionData {

	@XmlElement(name = "motion_time")
	private List<MotionTime> motionTimes;

	@XmlTransient
	private THashMap<String, MotionTime> motionTimesMap = new THashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (MotionTime motion : motionTimes) {
			motionTimesMap.put(motion.getName(), motion);
		}
		motionTimes.clear();
		motionTimes = null;
	}

	public Collection<MotionTime> getMotionTimes() {
		return motionTimesMap.values();
	}

	public MotionTime getMotionTime(String name) {
		return motionTimesMap.get(name);
	}

	public int size() {
		return motionTimesMap.size();
	}
}
