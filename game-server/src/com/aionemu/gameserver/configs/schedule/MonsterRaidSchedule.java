package com.aionemu.gameserver.configs.schedule;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.commons.utils.xml.JAXBUtil;

/**
 * @author Whoop
 */
@XmlRootElement(name = "raid_schedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class MonsterRaidSchedule {

	@XmlElement(name = "raid", required = true)
	private List<Raid> monsterRaidsList;

	public List<Raid> getMonsterRaidsList() {
		return monsterRaidsList;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "raid")
	public static class Raid {

		@XmlAttribute(required = true)
		private int id;
		@XmlElement(name = "raidTime")
		private List<String> raidTimes;

		public int getRaidId() {
			return id;
		}

		public List<String> getRaidTimes() {
			return raidTimes;
		}
	}

	public static MonsterRaidSchedule load() {
		return JAXBUtil.deserialize(new File("./config/schedule/monster_raid_schedule.xml"), MonsterRaidSchedule.class);
	}
}
