package com.aionemu.gameserver.services.webshop;

/**
 * @author ViAl
 */
public class WebshopRequest {

	private int requestId;
	private String buyerName;
	private String receiverName;
	private int itemId;
	private long itemCount;

	/**
	 * @param buyerName
	 * @param receiverName
	 * @param itemId
	 * @param itemCount
	 */
	public WebshopRequest(int requestId, String buyerName, String receiverName, int itemId, long itemCount) {
		this.requestId = requestId;
		this.buyerName = buyerName;
		this.receiverName = receiverName;
		this.itemId = itemId;
		this.itemCount = itemCount;
	}

	public int getRequestId() {
		return requestId;
	}

	public String getBuyerName() {
		return buyerName;
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
