package com.aionemu.gameserver.dataholders;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;

/**
 * Object of this class is containing <tt>WorldMapTemplate</tt> objects for all world maps. World maps are defined in data/static_data/world_maps.xml
 * file.
 * 
 * @author Luno
 */
@XmlRootElement(name = "world_maps")
@XmlAccessorType(XmlAccessType.NONE)
public class WorldMapsData implements Iterable<WorldMapTemplate> {

	@XmlElement(name = "map")
	private List<WorldMapTemplate> worldMaps;

	@XmlTransient
	private final Map<Integer, WorldMapTemplate> mapsById = new LinkedHashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (WorldMapTemplate map : worldMaps) {
			mapsById.put(map.getMapId(), map);
		}
		worldMaps = null;
	}

	@Override
	public Iterator<WorldMapTemplate> iterator() {
		return mapsById.values().iterator();
	}

	public void forEachParalllel(Consumer<WorldMapTemplate> consumer) {
		mapsById.values().parallelStream().forEach(consumer);
	}

	public int size() {
		return mapsById.size();
	}

	public WorldMapTemplate getTemplate(int worldId) {
		return mapsById.get(worldId);
	}

	public int getWorldIdByCName(String name) {
		for (WorldMapTemplate template : mapsById.values()) {
			if (template.getCName().equalsIgnoreCase(name)) {
				return template.getMapId();
			}
		}
		return 0;
	}
}
