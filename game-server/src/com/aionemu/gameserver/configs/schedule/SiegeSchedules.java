package com.aionemu.gameserver.configs.schedule;

import java.io.File;
import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.utils.xml.JAXBUtil;

/**
 * @author SoulKeeper, Source, Estrayl
 */
@XmlRootElement(name = "siege_schedule")
@XmlAccessorType(XmlAccessType.FIELD)
public class SiegeSchedules {

	@XmlElement(name = "fortress", required = true)
	private List<Fortress> fortressesList;
	@XmlElement(name = "agent_fight", required = true)
	private List<AgentFight> agentFights;

	public List<Fortress> getFortresses() {
		return fortressesList;
	}

	public List<AgentFight> getAgentFights() {
		return agentFights;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	private static abstract class SiegeSchedule {

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

	@XmlRootElement(name = "fortress")
	public static class Fortress extends SiegeSchedule {
	}

	@XmlRootElement(name = "agent_fight")
	public static class AgentFight extends SiegeSchedule {
	}

	public static SiegeSchedules load() {
		return JAXBUtil.deserialize(new File("./config/schedule/siege_schedule.xml"), SiegeSchedules.class);
	}

}
