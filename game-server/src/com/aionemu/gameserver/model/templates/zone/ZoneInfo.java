package com.aionemu.gameserver.model.templates.zone;

import com.aionemu.gameserver.model.geometry.Area;

/**
 * @author MrPoke
 */
public class ZoneInfo {

	private Area area;
	private ZoneTemplate zoneTemplate;

	/**
	 * @param area
	 * @param zoneTemplate
	 */
	public ZoneInfo(Area area, ZoneTemplate zoneTemplate) {
		this.area = area;
		this.zoneTemplate = zoneTemplate;
	}

	/**
	 * @return the area
	 */
	public Area getArea() {
		return area;
	}

	/**
	 * @return the zoneTemplate
	 */
	public ZoneTemplate getZoneTemplate() {
		return zoneTemplate;
	}
}
