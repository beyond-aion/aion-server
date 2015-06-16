package com.aionemu.loginserver.service.ptransfer;

import com.aionemu.loginserver.model.Account;

/**
 * @author KID
 */
public class PlayerTransferRequest {
	public PlayerTransferStatus status;
	public byte serverId;
	public byte targetServerId;
	public Account targetAccount;
	public byte[] db;
	public String name;
	public int targetAccountId;
	public int playerId;
	public Account account;
	public Account saccount;
	public int taskId;

	public PlayerTransferRequest(PlayerTransferStatus status) {
		this.status = status;
	}
}
