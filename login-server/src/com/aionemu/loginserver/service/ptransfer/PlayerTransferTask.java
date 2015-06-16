package com.aionemu.loginserver.service.ptransfer;

/**
 * @author KID
 */
public class PlayerTransferTask {
	public int sourceAccountId, targetAccountId, playerId;
	public byte sourceServerId, targetServerId;
	public int id;
	public byte status;
	public String comment;
	
	public final static byte STATUS_WAIT = 0, STATUS_ACTIVE = 1, STATUS_DONE = 2, STATUS_ERROR = 3;
}
