package com.aionemu.gameserver.spawnengine;

/**
 * @author Rolandas
 */
public class WalkerGroupShift {

	private float sagittalShift; // left and right (sides)
	private float coronalShift; // or dorsoventral (back and front)

	public static final float DISTANCE = 2; // 2 meters distance by default

	public WalkerGroupShift(float leftRight, float backFront) {
		sagittalShift = leftRight;
		coronalShift = backFront;
	}

	/**
	 * left and right (sides)
	 * 
	 * @return the sagittalShift
	 */
	public float getSagittalShift() {
		return sagittalShift;
	}

	/**
	 * dorsoventral (back and front)
	 * 
	 * @return the coronalShift
	 */
	public float getCoronalShift() {
		return coronalShift;
	}

	public void set(WalkerGroupShift shift) {
		this.sagittalShift = shift.sagittalShift;
		this.coronalShift = shift.coronalShift;
	}
}
