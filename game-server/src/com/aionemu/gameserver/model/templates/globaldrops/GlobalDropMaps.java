package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.ArrayList;
import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

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
