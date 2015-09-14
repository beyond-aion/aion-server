package com.aionemu.gameserver.model.templates.globaldrops;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

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
