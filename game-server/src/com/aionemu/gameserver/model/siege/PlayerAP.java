package com.aionemu.gameserver.model.siege;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;

/**
 * @author antness
 */
public class PlayerAP implements Comparable<PlayerAP> {

	private Player player;
	private Race race;
	private int ap;

	public PlayerAP(Player player) {
		this.player = player;
		this.race = player.getRace();
		this.ap = 0;
	}

	public Player getPlayer() {
		return this.player;
	}

	public Race getRace() {
		return this.race;
	}

	public int getAP() {
		return this.ap;
	}

	public void increaseAP(int ap) {
		this.ap += ap;
	}

	@Override
	public int compareTo(PlayerAP pl) {
		return this.ap - pl.ap;
	}
}
