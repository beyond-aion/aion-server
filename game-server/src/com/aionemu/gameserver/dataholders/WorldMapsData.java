package com.aionemu.gameserver.dataholders;

import java.util.Iterator;
import java.util.List;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.aionemu.gameserver.model.templates.world.WorldMapTemplate;

import gnu.trove.map.hash.TIntObjectHashMap;

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
	protected List<WorldMapTemplate> worldMaps;

	protected TIntObjectHashMap<WorldMapTemplate> worldIdMap = new TIntObjectHashMap<>();

	protected void afterUnmarshal(Unmarshaller u, Object parent) {
		for (WorldMapTemplate map : worldMaps) {
			worldIdMap.put(map.getMapId(), map);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public Iterator<WorldMapTemplate> iterator() {
		return worldMaps.iterator();
	}

	/**
	 * Returns the count of maps.
	 * 
	 * @return worldMaps.size()
	 */
	public int size() {
		return worldMaps == null ? 0 : worldMaps.size();
	}

	/**
	 * @param worldId
	 * @return
	 */
	public WorldMapTemplate getTemplate(int worldId) {
		return worldIdMap.get(worldId);
	}

	public int getWorldIdByCName(String name) {
		for (WorldMapTemplate template : worldMaps) {
			if (template.getCName().equalsIgnoreCase(name)) {
				return template.getMapId();
			}
		}
		return 0;
	}
}
