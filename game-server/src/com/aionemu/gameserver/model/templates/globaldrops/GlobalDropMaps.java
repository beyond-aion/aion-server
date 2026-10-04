package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropMaps")
public class GlobalDropMaps {

	@XmlElement(name = "gd_map")
	protected List<GlobalDropMap> gdMaps;

	public List<GlobalDropMap> getGlobalDropMaps() {
		if (gdMaps == null) {
			gdMaps = new ArrayList<>();
		}
		return this.gdMaps;
	}

}
