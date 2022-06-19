package com.aionemu.gameserver.model.items.storage;

/**
 * @author kosyachok, IlBuono
 */
public enum StorageType {
	CUBE(0, 27, 9, 102),
	REGULAR_WAREHOUSE(1, 24, 8),
	ACCOUNT_WAREHOUSE(2, 16, 8),
	LEGION_WAREHOUSE(3, 56, 8),
	PET_BAG_6(32, 6, 6),
	PET_BAG_12(33, 12, 6),
	PET_BAG_18(34, 18, 6),
	PET_BAG_24(35, 24, 6),
	CASH_PET_BAG_12(36, 12, 6),
	CASH_PET_BAG_18(37, 18, 6),
	CASH_PET_BAG_30(38, 30, 6),
	CASH_PET_BAG_24(39, 24, 6),
	PET_BAG_30(40, 30, 6),
	CASH_PET_BAG_26(41, 26, 6),
	CASH_PET_BAG_32(42, 32, 6),
	CASH_PET_BAG_34(43, 34, 6),
	HOUSE_STORAGE_01(60, 9, 9), // Plain 1-Drawer Cabinet
	HOUSE_STORAGE_02(61, 9, 9), // Simple 1-Drawer Cabinet
	HOUSE_STORAGE_03(62, 9, 9), // Clean 1-Drawer Cabinet
	HOUSE_STORAGE_04(63, 9, 9), // Convenient 1-Drawer Cabinet
	HOUSE_STORAGE_05(64, 9, 9), // Strong 1-Drawer Cabinet
	HOUSE_STORAGE_06(65, 9, 9), // Firm 1-Drawer Cabinet
	HOUSE_STORAGE_07(66, 9, 9), // Fine 1-Drawer Cabinet
	HOUSE_STORAGE_08(67, 9, 9), // Decorated 1-Drawer Cabinet
	HOUSE_STORAGE_09(68, 18, 9), // Small 2-Drawer Cabinet
	HOUSE_STORAGE_10(69, 18, 9), // Simple 2-Drawer Cabinet
	HOUSE_STORAGE_11(70, 18, 9), // Clean 2-Drawer Cabinet
	HOUSE_STORAGE_12(71, 18, 9), // Convenient 2-Drawer Cabinet
	HOUSE_STORAGE_13(72, 18, 9), // Strong 2-Drawer Cabinet
	HOUSE_STORAGE_14(73, 18, 9), // Firm 2-Drawer Cabinet
	HOUSE_STORAGE_15(74, 27, 9), // Spacious 3-Drawer Cabinet
	HOUSE_STORAGE_16(75, 27, 9), // Simple 3-Drawer Cabinet
	HOUSE_STORAGE_17(76, 27, 9), // Clean 3-Drawer Cabinet
	HOUSE_STORAGE_18(77, 27, 9), // Convenient 3-Drawer Cabinet
	HOUSE_STORAGE_19(78, 27, 9), // Strong 3-Drawer Cabinet
	HOUSE_STORAGE_20(79, 27, 9), // Firm 3-Drawer Cabinet
	BROKER(126),
	MAILBOX(127);

	public static final int PET_BAG_MIN = 32;
	public static final int PET_BAG_MAX = 43;
	public static final int HOUSE_WH_MIN = 60;
	public static final int HOUSE_WH_MAX = 79; // Custom cabinets ?? // since 3.0 to 4.0

	private int id;
	private int limit;
	private int length;
	private int specialLimit;

	private StorageType(int id, int limit, int length, int specialLimit) {
		this(id, limit, length);
		this.specialLimit = specialLimit;
	}

	private StorageType(int id, int limit, int length) {
		this(id);
		this.limit = limit;
		this.length = length;
	}

	private StorageType(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public int getLimit() {
		return limit;
	}

	public int getLength() {
		return length;
	}

	public int getSpecialLimit() {
		return specialLimit;
	}

	public static StorageType getStorageTypeById(int id) {
		for (StorageType st : values()) {
			if (st.id == id)
				return st;
		}
		return null;
	}

	public static int getStorageId(int limit, int length) {
		for (StorageType st : values()) {
			if (st.limit == limit && st.length == length)
				return st.id;
		}
		return -1;
	}
}
