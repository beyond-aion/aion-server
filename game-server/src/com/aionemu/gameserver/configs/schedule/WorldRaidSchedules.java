package com.aionemu.gameserver.configs.schedule;

import com.aionemu.commons.utils.xml.JAXBUtil;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlRootElement;
import java.io.File;
import java.util.List;

/**
 * @author Whoop, Sykra
 */
@XmlRootElement(name = "world_raid_schedules")
@XmlAccessorType(XmlAccessType.FIELD)
public class WorldRaidSchedules {

	@XmlElement(name = "world_raid_schedule", required = true)
	private List<WorldRaidSchedule> worldRaidSchedules;

	public List<WorldRaidSchedule> getWorldRaidSchedules() {
		return worldRaidSchedules;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "world_raid_schedule")
	public static class WorldRaidSchedule {

		@XmlAttribute(name = "id", required = true)
		private String id = "";

		@XmlAttribute(name = "min_count")
		private int minCount = 0;

		@XmlAttribute(name = "max_count")
		private int maxCount;

		@XmlAttribute(name = "is_special_raid")
		private boolean isSpecialRaid;

		@XmlList
		@XmlAttribute(name = "locations", required = true)
		private List<Integer> locations;

		@XmlElement(name = "start_time")
		private List<String> raidTimes;

		public String getId() {
			return id;
		}

		public int getMinCount() {
			return minCount;
		}

		public int getMaxCount() {
			return maxCount;
		}

		public boolean isSpecialRaid() {
			return isSpecialRaid;
		}

		public List<Integer> getLocations() {
			return locations;
		}

		public List<String> getRaidTimes() {
			return raidTimes;
		}

	}

	public static WorldRaidSchedules load() {
		return JAXBUtil.deserialize(new File("./config/schedule/world_raid_schedule.xml"), WorldRaidSchedules.class);
	}

}
