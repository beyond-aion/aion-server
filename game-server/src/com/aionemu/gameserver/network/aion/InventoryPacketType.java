package com.aionemu.gameserver.network.aion;

/**
 * @author ATracer
 */
public enum InventoryPacketType {

	WAREHOUSE(false, false, false),
	INVENTORY(true, false, false),
	MAIL_REPURCHASE(false, true, false),
	PRIVATE_STORE(false, false, true),
	WEAPON_SWITCH(true, false, false, true);

	private boolean isInventory;
	private boolean isMailOrRepurchase;
	private boolean isPrivateStore;
	private boolean isWeaponSwitch;

	private InventoryPacketType(boolean isInventory, boolean isMail, boolean isPrivateStore) {
		this(isInventory, isMail, isPrivateStore, false);
	}

	private InventoryPacketType(boolean isInventory, boolean isMail, boolean isPrivateStore, boolean isWeaponSwitch) {
		this.isInventory = isInventory;
		this.isMailOrRepurchase = isMail;
		this.isPrivateStore = isPrivateStore;
		this.isWeaponSwitch = isWeaponSwitch;
	}

	public final boolean isInventory() {
		return isInventory;
	}

	public final boolean isMail() {
		return isMailOrRepurchase;
	}

	public final boolean isRepurchase() {
		return isMailOrRepurchase;
	}

	public final boolean isPrivateStore() {
		return isPrivateStore;
	}

	public final boolean isWeaponSwitch() {
		return isWeaponSwitch;
	}

}
