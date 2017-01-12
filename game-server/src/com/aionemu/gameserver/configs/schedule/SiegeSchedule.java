package com.aionemu.gameserver.configs.schedule;

import java.io.File;
import java.nio.charset.StandardCharsets;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import org.apache.commons.io.FileUtils;

import com.aionemu.commons.utils.xml.JAXBUtil;

/**
 * @author SoulKeeper, Source
 * @modified Estrayl
 *           TODO: Remove code redundancy
 */
@XmlRootElement(name = "siege_schedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiegeSchedule {

	@XmlElement(name = "fortress", required = true)
	private List<Fortress> fortressesList;
	@XmlElement(name = "agent_fight", required = true)
	private List<AgentFight> agentFights;

	public List<Fortress> getFortressesList() {
		return fortressesList;
	}

	public List<AgentFight> getAgentFights() {
		return agentFights;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "fortress")
	public static class Fortress {

		@XmlAttribute(required = true)
		private int id;
		@XmlElement(name = "siegeTime", required = true)
		private List<String> siegeTimes;

		public int getId() {
			return id;
		}

		public List<String> getSiegeTimes() {
			return siegeTimes;
		}
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlRootElement(name = "agent_fight")
	public static class AgentFight {

		@XmlAttribute(required = true)
		private int id;
		@XmlElement(name = "siegeTime", required = true)
		private List<String> siegeTime;

		public int getId() {
			return id;
		}

		public List<String> getSiegeTimes() {
			return siegeTime;
		}
	}

	public static SiegeSchedule load() {
		SiegeSchedule ss;
		try {
			String xml = FileUtils.readFileToString(new File("./config/schedule/siege_schedule.xml"), StandardCharsets.UTF_8);
			ss = JAXBUtil.deserialize(xml, SiegeSchedule.class);
		} catch (Exception e) {
			throw new RuntimeException("Failed to initialize sieges", e);
		}
		return ss;
	}

}
