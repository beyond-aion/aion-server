package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.skillengine.model.MotionTime;


/**
 * @author kecimis
 */
@XmlRootElement(name = "motion_times")
@XmlAccessorType(XmlAccessType.FIELD)
public class MotionData {

	@XmlElement(name = "motion_time")
	private List<MotionTime> motionTimes;

	@XmlTransient
	private final Map<String, MotionTime> motionTimesMap = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (MotionTime motion : motionTimes) {
			motionTimesMap.put(motion.getName(), motion);
		}
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
