package com.aionemu.gameserver.questEngine.handlers.models;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Hilgert
 * @modified Pad
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcInfos")
public class NpcInfos {

	@XmlAttribute(name = "npc_ids", required = true)
	protected List<Integer> npcIds;
	
	@XmlAttribute(name = "quest_dialog")
	protected int dialogId;
	
	@XmlAttribute(name = "close_dialog")
	protected int closeDialogId;
	
	@XmlAttribute(name = "movie")
	protected int movie;

	/**
	 * Gets the value of the npcIds property.
	 */
	public List<Integer> getNpcIds() {
		return npcIds;
	}

	/**
	 * Gets the value of the DialogAction property.
	 */
	public int getQuestDialog() {
		return dialogId;
	}

	/**
	 * Gets the value of the closeDialog property.
	 */
	public int getCloseDialog() {
		return closeDialogId;
	}

	/**
	 * @return the movie
	 */
	public int getMovie() {
		return movie;
	}

	/**
	 * @param movie
	 *          the movie to set
	 */
	public void setMovie(int movie) {
		this.movie = movie;
	}
}
