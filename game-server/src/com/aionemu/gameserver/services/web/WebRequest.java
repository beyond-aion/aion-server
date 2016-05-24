package com.aionemu.gameserver.services.web;

/**
 * Created on 24.05.2016
 * 
 * @author Estrayl
 */
public class WebRequest {

	private String receiverName;
	private int requestId, itemId;
	private long itemCount;

	public WebRequest(int requestId, String receiverName, int itemId, long itemCount) {
		this.requestId = requestId;
		this.receiverName = receiverName;
		this.itemId = itemId;
		this.itemCount = itemCount;
	}

	public int getRequestId() {
		return requestId;
	}

	public String getReceiverName() {
		return receiverName;
	}

	public int getItemId() {
		return itemId;
	}

	public long getItemCount() {
		return itemCount;
	}
}
