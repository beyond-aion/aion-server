package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Hilgert, Pad, Neon
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcInfos")
public class NpcInfos {

	@XmlAttribute(name = "npc_ids", required = true)
	private List<Integer> npcIds;
	
	@XmlAttribute(name = "movie")
	private int movie;

	/**
	 * Gets the value of the npcIds property.
	 */
	public List<Integer> getNpcIds() {
		return npcIds;
	}

	/**
	 * @return the movie
	 */
	public int getMovie() {
		return movie;
	}
}
