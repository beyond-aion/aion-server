package com.aionemu.gameserver.model.templates.globaldrops;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropMap")
public class GlobalDropMap {

	@XmlAttribute(name = "map_id", required = true)
	protected int mapId;

	public int getMapId() {
		return mapId;
	}
}
