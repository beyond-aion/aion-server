package com.aionemu.loginserver.service.ptransfer;

/**
 * @author KID
 */
public enum PlayerTransferResultStatus {
	SEND_INFO(20),
	OK(21),
	ERROR(22), 
	PERFORM_ACTION(23);
	
	private int id;
	
	public int getId() {
		return id;
	}
	
	PlayerTransferResultStatus(int id) {
		this.id = id;
	}
}
