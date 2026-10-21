package com.aionemu.gameserver.model.gameobjects.player;

import com.aionemu.gameserver.model.items.storage.StorageType;

record StorageManager(StorageType storageType, int objectId) {

	public boolean matches(StorageType storageType) {
		return this.storageType == storageType || storageType == StorageType.ACCOUNT_WAREHOUSE && this.storageType == StorageType.REGULAR_WAREHOUSE;
	}
}
