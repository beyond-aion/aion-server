package com.aionemu.gameserver.questEngine.handlers.models;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

/**
 * @author Hilgert
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "NpcInfos")
public class NpcInfos {

	@XmlAttribute(name = "npc_id", required = true)
	protected int npcId;
	@XmlAttribute(name = "var", required = true)
	protected int var;
	@XmlAttribute(name = "quest_dialog", required = true)
	protected int DialogAction;
	@XmlAttribute(name = "close_dialog")
	protected int closeDialog;
	@XmlAttribute(name = "movie")
	protected int movie;

	/**
	 * Gets the value of the npcId property.
	 */
	public int getNpcId() {
		return npcId;
	}

	/**
	 * Gets the value of the var property.
	 */
	public int getVar() {
		return var;
	}

	/**
	 * Gets the value of the DialogAction property.
	 */
	public int getQuestDialog() {
		return DialogAction;
	}

	/**
	 * Gets the value of the closeDialog property.
	 */
	public int getCloseDialog() {
		return closeDialog;
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
