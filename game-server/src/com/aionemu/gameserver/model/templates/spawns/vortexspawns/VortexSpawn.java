package com.aionemu.gameserver.model.templates.spawns.vortexspawns;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.vortex.VortexStateType;

/**
 * @author Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "VortexSpawn")
public class VortexSpawn {

	@XmlAttribute(name = "id")
	private int id;

	public int getId() {
		return id;
	}

	@XmlElement(name = "state_type")
	private List<VortexStateTemplate> stateTemplates;

	public List<VortexStateTemplate> getSiegeModTemplates() {
		return stateTemplates;
	}

	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "VortexStateTemplate")
	public static class VortexStateTemplate {

		@XmlElement(name = "spawn")
		private List<Spawn> spawns;
		@XmlAttribute(name = "state")
		private VortexStateType stateType;

		public List<Spawn> getSpawns() {
			return spawns;
		}

		public VortexStateType getStateType() {
			return stateType;
		}

	}

}
