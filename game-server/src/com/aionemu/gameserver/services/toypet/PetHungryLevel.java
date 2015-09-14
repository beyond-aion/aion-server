package com.aionemu.gameserver.services.toypet;

/**
 * @author Rolandas
 */
public enum PetHungryLevel {
	HUNGRY(0),
	CONTENT(1),
	SEMIFULL(2),
	FULL(3);

	private byte value;

	PetHungryLevel(int value) {
		this.value = (byte) value;
	}

	/**
	 * @return the value
	 */
	public byte getValue() {
		return value;
	}

	public PetHungryLevel getNextValue() {
		byte levelValue = value;
		switch (levelValue) {
			case 0:
				return CONTENT;
			case 1:
				return SEMIFULL;
			case 2:
				return FULL;
			case 3:
				return HUNGRY;
			default:
				return HUNGRY;
		}
	}

	public static PetHungryLevel fromId(int value) {
		return PetHungryLevel.values()[value];
	}

}
