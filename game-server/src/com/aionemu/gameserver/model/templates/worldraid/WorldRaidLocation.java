package com.aionemu.gameserver.model.templates.worldraid;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Alcapwnd, Whoop, Sykra
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "WorldRaidLocation")
public class WorldRaidLocation {

	@XmlElementWrapper(name = "world_raid_npcs", required = true)
	@XmlElement(name = "world_raid_npc", required = true)
	private List<WorldRaidNpc> npcPool;

	@XmlElementWrapper(name = "location_markers", required = true)
	@XmlElement(name = "spot", required = true)
	private List<MarkerSpot> locationMarkers;

	@XmlAttribute(name = "location_id", required = true)
	private int locationId;

	@XmlAttribute(name = "map_id", required = true)
	private int mapId;

	@XmlAttribute(name = "x", required = true)
	private float x;

	@XmlAttribute(name = "y", required = true)
	private float y;

	@XmlAttribute(name = "z", required = true)
	private float z;

	@XmlAttribute(name = "h")
	private byte h = 0;

	public int getLocationId() {
		return locationId;
	}

	public int getMapId() {
		return mapId;
	}

	public float getX() {
		return x;
	}

	public float getY() {
		return y;
	}

	public float getZ() {
		return z;
	}

	public byte getH() {
		return h;
	}

	public List<WorldRaidNpc> getNpcPool() {
		return npcPool;
	}

	public List<MarkerSpot> getLocationMarkers() {
		return locationMarkers;
	}

}
