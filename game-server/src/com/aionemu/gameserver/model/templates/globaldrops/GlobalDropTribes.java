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
@XmlType(name = "GlobalDropTribes")
public class GlobalDropTribes {

	@XmlElement(name = "gd_tribe")
	protected List<GlobalDropTribe> gdTribes;

	public List<GlobalDropTribe> getGlobalDropTribes() {
		if (gdTribes == null) {
			gdTribes = new ArrayList<>();
		}
		return this.gdTribes;
	}

}
