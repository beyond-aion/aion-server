package com.aionemu.gameserver.model.templates.spawns.panesterra;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.services.panesterra.ahserion.PanesterraFaction;

/**
 * @author Yeats
 *
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "AhserionsFlightSpawn")
public class AhserionsFlightSpawn {
	
	@XmlElement(name = "ahserion_stage_spawn")
	private List<AhserionStageSpawnTemplate> ahserionStageSpawnTemplate;
	@XmlAttribute(name = "faction")
	private PanesterraFaction faction;
	
	public PanesterraFaction getFaction() {
		return faction;
	}
	
	public List<AhserionStageSpawnTemplate> getStageSpawnTemplate() {
		return ahserionStageSpawnTemplate;
	}
	
	@XmlAccessorType(XmlAccessType.FIELD)
	@XmlType(name = "AhserionStageSpawnTemplate")
	public static class AhserionStageSpawnTemplate {
		
		@XmlElement(name = "spawn")
		private List<Spawn> spawns;
		@XmlAttribute(name = "stage")
		private int stage = 0;
		
		public int getStage() {
			return stage;
		}
		
		public List<Spawn> getSpawns() {
			return spawns;
		}
	}
}
