package com.aionemu.gameserver.services.rewardPackage;


/**
 * Created on 29.05.2016
 * 
 * @author Estrayl
 * @since AION 4.8
 */
public class PackageItem {

	private final int itemId;
	private final int itemCount;
	
	public PackageItem(int itemId, int itemCount) {
		this.itemId = itemId;
		this.itemCount = itemCount;
	}
	
	public int getId() {
		return itemId;
	}
	
	public int getCount() {
		return itemCount;
	}
}
