package com.aionemu.gameserver.services.panesterra.ahserion;

/**
 * Created on October 29th, 2017.
 * 
 * @author Estrayl
 * @since Beyond AION 4.8
 */
public enum PanesterraFaction {

	BELUS(69),
	ASPIDA(70),
	ATANATOS(71),
	DISILLON(72),
	BALAUR(1);

	private int factionId;

	private PanesterraFaction(int factionId) {
		this.factionId = factionId;
	}

	public int getId() {
		return factionId;
	}
}
