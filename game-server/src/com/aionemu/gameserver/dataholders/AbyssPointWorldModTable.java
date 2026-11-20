package com.aionemu.gameserver.dataholders;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import jakarta.xml.bind.Unmarshaller;
import jakarta.xml.bind.annotation.*;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.templates.abyss.AbyssPointWorldMod;

/**
 * Per-world AP gain and loss modifiers; worlds without an entry use 100%.
 */
@XmlRootElement(name = "abysspoint_world_mods")
@XmlAccessorType(XmlAccessType.NONE)
public class AbyssPointWorldModTable {

	@XmlElement(name = "world_mod")
	private List<AbyssPointWorldMod> mods;

	private final Map<Integer, AbyssPointWorldMod> byMap = new HashMap<>();

	@SuppressWarnings("unused")
	private void afterUnmarshal(Unmarshaller unmarshaller, Object parent) {
		for (AbyssPointWorldMod mod : mods)
			byMap.put(mod.getMapId(), mod);
		mods = null;
	}

	/**
	 * @return AP gain modifier of the world, 1 if the world has no modifier.
	 */
	public float getDropPointMod(int mapId, Race race) {
		AbyssPointWorldMod mod = byMap.get(mapId);
		return mod == null ? 1 : mod.getDropPointMod(race);
	}

	/**
	 * @return AP loss modifier of the world, 1 if the world has no modifier.
	 */
	public float getDeathPenaltyMod(int mapId, Race race) {
		AbyssPointWorldMod mod = byMap.get(mapId);
		return mod == null ? 1 : mod.getDeathPenaltyMod(race);
	}

	public int size() {
		return byMap.size();
	}
}
