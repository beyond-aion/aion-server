package com.aionemu.gameserver.model.templates.spawns.basespawns;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.spawns.Spawn;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "BaseSpawn")
public class BaseSpawn {

	@XmlAttribute(name = "id")
	private int id;
	@XmlAttribute(name = "world")
	private int world;
	@XmlElement(name = "simple_race")
	private List<SimpleRaceTemplate> simpleRaceTemplates;

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return world;
	}

	public List<SimpleRaceTemplate> getBaseRaceTemplates() {
		return simpleRaceTemplates;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "BaseRaceTemplate")
	public static class SimpleRaceTemplate {

		@XmlAttribute(name = "race")
		private Race race;

		public Race getBaseRace() {
			return race;
		}

		@XmlElement(name = "spawn")
		private List<Spawn> spawns;

		public List<Spawn> getSpawns() {
			return spawns;
		}

	}

}
