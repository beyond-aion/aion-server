package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;

import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorTemplate;
import com.aionemu.gameserver.model.templates.staticdoor.StaticDoorWorld;

/**
 * @author Wakizashi
 */
@XmlRootElement(name = "staticdoor_templates")
@XmlAccessorType(XmlAccessType.FIELD)
public class StaticDoorData {

	@XmlElement(name = "world")
	private List<StaticDoorWorld> staticDoorWorlds;
	@XmlTransient
	private Map<Integer, StaticDoorWorld> doorWorlds;

	void afterUnmarshal(Unmarshaller u, Object parent) {
		doorWorlds = new HashMap<>();
		for (StaticDoorWorld world : staticDoorWorlds) {
			if (doorWorlds.putIfAbsent(world.getWorldId(), world) != null)
				throw new IllegalArgumentException("Duplicate static door world " + world.getWorldId());
		}
		staticDoorWorlds = null;
	}

	public int size() {
		return doorWorlds.size();
	}

	public Collection<StaticDoorTemplate> getStaticDoors(int worldId) {
		StaticDoorWorld doorWorld = doorWorlds.get(worldId);
		return doorWorld == null ? Collections.emptyList() : doorWorld.getStaticDoors();
	}

	public StaticDoorTemplate getStaticDoor(int worldId, int staticId) {
		StaticDoorWorld doorWorld = doorWorlds.get(worldId);
		return doorWorld == null ? null : doorWorld.getStaticDoor(staticId);
	}
}
