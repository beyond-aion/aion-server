package com.aionemu.gameserver.services.toypet;

/**
 * @author Rolandas
 */
public enum PetHungryLevel {
	HUNGRY(0),
	CONTENT(1),
	SEMIFULL(2),
	FULL(3);

	private final byte value;

	PetHungryLevel(int value) {
		this.value = (byte) value;
	}

	public byte getValue() {
		return value;
	}

	public PetHungryLevel getNextValue() {
		return switch (value) {
			case 0 -> CONTENT;
			case 1 -> SEMIFULL;
			case 2 -> FULL;
			default -> HUNGRY;
		};
	}

	public static PetHungryLevel fromId(int value) {
		return PetHungryLevel.values()[value];
	}
}