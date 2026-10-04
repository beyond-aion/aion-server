package com.aionemu.gameserver.model.templates.globaldrops;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.templates.npc.GroupDropType;

/**
 * @author Bobobear
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropNpcGroup")
public class GlobalDropNpcGroup {

	@XmlAttribute(name = "group", required = true)
	protected GroupDropType group;

	public GroupDropType getGroup() {
		return group;
	}
}
