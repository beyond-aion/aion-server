package com.aionemu.gameserver.dataholders;

import gnu.trove.map.hash.THashMap;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.skillengine.model.MotionTime;

/**
 * @author kecimis
 */
@XmlRootElement(name = "motion_times")
@XmlAccessorType(XmlAccessType.FIELD)
public class MotionData {

	@XmlElement(name = "motion_time")
	protected List<MotionTime> motionTimes;

	@XmlTransient
	private THashMap<String, MotionTime> motionTimesMap = new THashMap<String, MotionTime>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (MotionTime motion : motionTimes) {
			motionTimesMap.put(motion.getName(), motion);
		}
	}

	/**
	 * @return the motionTimeList
	 */
	public List<MotionTime> getMotionTimes() {
		if (motionTimes == null)
			motionTimes = new ArrayList<MotionTime>();

		return motionTimes;
	}

	public MotionTime getMotionTime(String name) {
		return motionTimesMap.get(name);
	}

	public int size() {
		if (motionTimes == null)
			return 0;

		return motionTimes.size();
	}
}
