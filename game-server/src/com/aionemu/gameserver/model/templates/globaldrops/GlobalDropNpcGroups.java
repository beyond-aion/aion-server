package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlType;

import javolution.util.FastTable;

/**
 * @author bobobear
 */

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropNpcGroups")
public class GlobalDropNpcGroups {

	@XmlElement(name = "gd_npc_group")
	protected List<GlobalDropNpcGroup> gdNpcGroups;

	public List<GlobalDropNpcGroup> getGlobalDropNpcGroups() {
		if (gdNpcGroups == null) {
			gdNpcGroups = new FastTable<>();
		}
		return this.gdNpcGroups;
	}

}
