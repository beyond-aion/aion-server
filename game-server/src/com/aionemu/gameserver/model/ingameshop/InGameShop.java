package com.aionemu.gameserver.model.ingameshop;

/**
 * @author xTz
 */
public class InGameShop {

	private byte subCategory;
	private byte category = 2;

	public byte getSubCategory() {
		return subCategory;
	}

	public void setSubCategory(byte subCategory) {
		this.subCategory = subCategory;
	}

	public byte getCategory() {
		return category;
	}

	public void setCategory(byte category) {
		this.category = category;
	}

}
