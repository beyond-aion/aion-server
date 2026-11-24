package com.aionemu.gameserver.model.gameobjects;

/**
 * Pet mood requests, sent by the client in {@code C_FUNCTIONAL_PET} action 12.
 */
public enum PetMoodRequest {

	STATUS(0),
	SOCIAL(1),
	CANCEL(2),
	PRESENT(3);

	private final int id;

	PetMoodRequest(int id) {
		this.id = id;
	}

	public int getId() {
		return id;
	}

	public static PetMoodRequest getById(int id) {
		for (PetMoodRequest request : values()) {
			if (request.getId() == id)
				return request;
		}
		return null;
	}
}
