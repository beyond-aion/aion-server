package com.aionemu.gameserver.model.autogroup;

/**
 * @author xTz
 */
public enum EntryRequestType {
	NEW_GROUP_ENTRY((byte) 0),
	QUICK_GROUP_ENTRY((byte) 1),
	GROUP_ENTRY((byte) 2);

	private byte id;

	private EntryRequestType(byte id) {
		this.id = id;
	}

	public byte getId() {
		return id;
	}

	public boolean isQuickGroupEntry() {
		return id == 1;
	}

	public boolean isGroupEntry() {
		return id == 2;
	}

	public static EntryRequestType getTypeById(byte id) {
		for (EntryRequestType ert : values()) {
			if (ert.getId() == id) {
				return ert;
			}
		}
		return null;
	}
}
