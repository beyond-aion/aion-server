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
