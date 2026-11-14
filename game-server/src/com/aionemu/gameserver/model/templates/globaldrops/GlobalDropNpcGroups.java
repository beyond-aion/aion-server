package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.List;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlElement;
import jakarta.xml.bind.annotation.XmlType;

/**
 * @author bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropNpcGroups")
public class GlobalDropNpcGroups {

	@XmlElement(name = "gd_npc_group")
	protected List<GlobalDropNpcGroup> gdNpcGroups;

	public List<GlobalDropNpcGroup> getGlobalDropNpcGroups() {
		return gdNpcGroups == null ? List.of() : gdNpcGroups;
	}
}
