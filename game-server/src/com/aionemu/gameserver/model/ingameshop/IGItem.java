package com.aionemu.gameserver.model.ingameshop;

/**
 * @author xTz
 */
public class IGItem {

	private int objectId;
	private int itemId;
	private long itemCount;
	private long itemPrice;
	private byte category;
	private byte subCategory;
	private int list;
	private int salesRanking;
	private byte itemType;
	private byte gift;
	private String titleDescription;
	private String itemDescription;

	public IGItem(int objectId, int itemId, long itemCount, long itemPrice, byte category, byte subCategory, int list, int salesRanking, byte itemType,
		byte gift, String titleDescription, String itemDescription) {
		this.objectId = objectId;
		this.itemId = itemId;
		this.itemCount = itemCount;
		this.itemPrice = itemPrice;
		this.category = category;
		this.subCategory = subCategory;
		this.list = list;
		this.salesRanking = salesRanking;
		this.itemType = itemType;
		this.gift = gift;
		this.titleDescription = titleDescription;
		this.itemDescription = itemDescription;
	}

	public int getObjectId() {
		return objectId;
	}

	public int getItemId() {
		return itemId;
	}

	public long getItemCount() {
		return itemCount;
	}

	public long getItemPrice() {
		return itemPrice;
	}

	public byte getCategory() {
		return category;
	}

	public byte getSubCategory() {
		return subCategory;
	}

	public int getList() {
		return list;
	}

	public int getSalesRanking() {
		return salesRanking;
	}

	public byte getItemType() {
		return itemType;
	}

	public byte getGift() {
		return gift;
	}

	public String getItemDescription() {
		return itemDescription;
	}

	public String getTitleDescription() {
		return titleDescription;
	}

	public void increaseSales() {
		salesRanking++;
	}
}
