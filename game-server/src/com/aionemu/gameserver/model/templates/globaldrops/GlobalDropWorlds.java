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
@XmlType(name = "GlobalDropWorlds")
public class GlobalDropWorlds {

	@XmlElement(name = "gd_world")
	protected List<GlobalDropWorld> gdWorlds;

	public List<GlobalDropWorld> getGlobalDropWorlds() {
		if (gdWorlds == null) {
			gdWorlds = new ArrayList<>();
		}
		return this.gdWorlds;
	}

}
