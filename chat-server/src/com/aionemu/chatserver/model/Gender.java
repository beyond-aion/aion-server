package com.aionemu.chatserver.model;

/**
 * @author ATracer
 */
public enum Gender {
	MALE(0),
	FEMALE(1);

	private int genderId;

	private Gender(int genderId) {
		this.genderId = genderId;
	}

	public int getGenderId() {
		return genderId;
	}
}
