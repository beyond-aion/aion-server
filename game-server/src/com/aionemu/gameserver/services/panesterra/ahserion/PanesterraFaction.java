package com.aionemu.gameserver.services.panesterra.ahserion;

/**
 * @author Estrayl
 */
public enum PanesterraFaction {

	BELUS(69),
	ASPIDA(70),
	ATANATOS(71),
	DISILLON(72),
	BALAUR(1);

	private final int factionId;

	PanesterraFaction(int factionId) {
		this.factionId = factionId;
	}

	public int getId() {
		return factionId;
	}
}
