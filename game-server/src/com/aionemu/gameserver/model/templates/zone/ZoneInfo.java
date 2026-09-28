package com.aionemu.gameserver.model.templates.zone;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.geometry.Area;

/**
 * @author MrPoke
 */
public class ZoneInfo {

	private final Area area;
	private final ZoneTemplate zoneTemplate;

	public ZoneInfo(ZoneTemplate zoneTemplate) {
		this.area = Area.create(zoneTemplate);
		this.zoneTemplate = zoneTemplate;
	}

	public Area getArea() {
		return area;
	}

	public ZoneTemplate getZoneTemplate() {
		return zoneTemplate;
	}

	public boolean matches(String zoneName) {
		if (zoneTemplate.getName().equals(zoneName))
			return true;
		DataManager.ZONE_DATA.validateZoneName(zoneName);
		return false;
	}
}
