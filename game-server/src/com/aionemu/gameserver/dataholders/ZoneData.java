package com.aionemu.gameserver.dataholders;

import java.util.*;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.templates.zone.ZoneClassName;
import com.aionemu.gameserver.model.templates.zone.ZoneInfo;
import com.aionemu.gameserver.model.templates.zone.ZoneTemplate;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "")
@XmlRootElement(name = "zones")
public class ZoneData {

	@XmlElement(name = "zone")
	public List<ZoneTemplate> zoneList;

	@XmlTransient
	private final Map<Integer, List<ZoneInfo>> zoneNameMap = new HashMap<>();

	@XmlTransient
	private final Set<String> zoneNames = new HashSet<>();

	@XmlTransient
	private final Map<ZoneTemplate, Integer> weatherZoneIds = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		int lastMapId = 0;
		int weatherZoneId = 1;
		for (ZoneTemplate zone : zoneList) {
			List<ZoneInfo> zones = zoneNameMap.computeIfAbsent(zone.getMapid(), _ -> new ArrayList<>());
			if (zone.getZoneType() == ZoneClassName.WEATHER) {
				if (lastMapId != zone.getMapid()) {
					lastMapId = zone.getMapid();
					weatherZoneId = 1;
				}
				weatherZoneIds.put(zone, weatherZoneId++);
			}
			ZoneInfo zoneInfo = new ZoneInfo(zone);
			zones.add(zoneInfo);
			if (!zoneNames.add(zoneInfo.getZoneTemplate().getName()))
				throw new IllegalArgumentException("Duplicate zone name: " + zoneInfo.getZoneTemplate().getName());
		}
		zoneList = null;
	}

	public List<ZoneInfo> getZones(int mapId) {
		return zoneNameMap.getOrDefault(mapId, Collections.emptyList());
	}

	public boolean validateZoneName(String zoneName) {
		if (!isValidZoneName(zoneName)) {
			LoggerFactory.getLogger(getClass()).warn("Missing data for zone: " + zoneName);
			return false;
		}
		return true;
	}

	public boolean isValidZoneName(String zoneName) {
		return zoneNames.contains(zoneName);
	}

	public int size() {
		return zoneNames.size();
	}

	/**
	 * Weather zone ID it's an order number (starts from 1)
	 */
	public int getWeatherZoneId(ZoneTemplate template) {
		Integer id = weatherZoneIds.get(template);
		if (id == null)
			return 0;
		return id;
	}
}
