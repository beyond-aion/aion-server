package com.aionemu.gameserver.services.transfers;

import com.aionemu.gameserver.model.gameobjects.player.Player;


/**
 * @author KID
 */
public class TransferablePlayer {
	public int playerId;
	public int accountId;
	public int targetAccountId;
	public Player player;
	public byte targetServerId;
	public int taskId;
	
	public TransferablePlayer(int playerId, int accountId, int targetAccountId) {
		this.playerId = playerId;
		this.accountId = accountId;
		this.targetAccountId = targetAccountId;
	}
}
