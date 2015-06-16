package com.aionemu.gameserver.model;

import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author synchro2
 */

public class Wedding {

	private Player player;
	private Player partner;
	private Player priest;
	private boolean accepted;

	public Wedding(Player player, Player partner, Player priest) {
		super();
		this.player = player;
		this.partner = partner;
		this.priest = priest;
	}

	public void setAccept() {
		this.accepted = true;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Player getPartner() {
		return this.partner;
	}

	public Player getPriest() {
		return this.priest;
	}

	public boolean isAccepted() {
		return this.accepted;
	}

}