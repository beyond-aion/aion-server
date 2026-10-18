package com.aionemu.gameserver.model.templates.globaldrops;

import java.util.Set;

import jakarta.xml.bind.annotation.XmlAccessType;
import jakarta.xml.bind.annotation.XmlAccessorType;
import jakarta.xml.bind.annotation.XmlAttribute;
import jakarta.xml.bind.annotation.XmlList;
import jakarta.xml.bind.annotation.XmlType;

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
