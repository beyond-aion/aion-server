package com.aionemu.gameserver.model.house;

import java.sql.Timestamp;

/**
 * @author Rolandas
 */
public class PlayerHouseBid {

	private int playerId;
	private int houseId;
	private long offer;
	private Timestamp time;

	public PlayerHouseBid(int playerId, int houseId, long offer, Timestamp time) {
		this.playerId = playerId;
		this.houseId = houseId;
		this.offer = offer;
		this.time = time;
	}

	public int getPlayerId() {
		return playerId;
	}

	public int getHouseId() {
		return houseId;
	}

	public long getBidOffer() {
		return offer;
	}

	public Timestamp getTime() {
		return time;
	}

}
