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
@XmlType(name = "GlobalDropZones")
public class GlobalDropZones {

	@XmlElement(name = "gd_zone")
	protected List<GlobalDropZone> gdZones;

	public List<GlobalDropZone> getGlobalDropZones() {
		if (gdZones == null) {
			gdZones = new ArrayList<>();
		}
		return this.gdZones;
	}

}
