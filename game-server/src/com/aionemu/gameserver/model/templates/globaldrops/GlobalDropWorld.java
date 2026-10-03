package com.aionemu.gameserver.model.templates.globaldrops;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.world.WorldDropType;

/**
 * @author AionCool
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropWorld")
public class GlobalDropWorld {

	@XmlAttribute(name = "wd_type", required = true)
	protected WorldDropType wdType;

	public WorldDropType getWorldDropType() {
		return wdType;
	}
}
