package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlList;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "GlobalDropExcludedNpcs")
public class GlobalDropExcludedNpcs {

	@XmlList
	@XmlAttribute(name = "npc_ids", required = true)
	private Set<Integer> npcIds;

	public Set<Integer> getNpcIds() {
		return npcIds;
	}

}
