package com.aionemu.gameserver.model.templates.spawns.basespawns;

import java.util.List;

import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.base.BaseOccupier;
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
	@XmlElement(name = "occupier_template")
	private List<BaseOccupierTemplate> baseOccupierTemplates;

	public int getId() {
		return id;
	}

	public int getWorldId() {
		return world;
	}

	public List<BaseOccupierTemplate> getOccupierTemplates() {
		return baseOccupierTemplates;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "BaseOccupierTemplate")
	public static class BaseOccupierTemplate {

		@XmlAttribute(name = "occupier")
		private BaseOccupier occupier;

		public BaseOccupier getOccupier() {
			return occupier;
		}

		@XmlElement(name = "spawn")
		private List<Spawn> spawns;

		public List<Spawn> getSpawns() {
			return spawns;
		}

	}

}
