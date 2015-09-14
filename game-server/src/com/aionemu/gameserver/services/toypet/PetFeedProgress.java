package com.aionemu.gameserver.services.toypet;

/**
 * @author Rolandas
 */
public final class PetFeedProgress {

	private int totalPoints = 0;
	private short regularConsumed = 0;
	private short lovedConsumed = 0;
	private PetHungryLevel hungryLevel = PetHungryLevel.HUNGRY;
	private short lovedFoodMax = 0;
	private boolean lovedFeeded = false;

	public PetFeedProgress(short lovedFoodLimit) {
		lovedFoodMax = (short) (lovedFoodLimit & 0x3F);
	}

	/**
	 * @return the totalPoints
	 */
	public int getTotalPoints() {
		return totalPoints;
	}

	public void setTotalPoints(int points) {
		totalPoints = points & 0x3FFF;
	}

	/**
	 * @return the hungryLevel
	 */
	public PetHungryLevel getHungryLevel() {
		return hungryLevel;
	}

	public void setHungryLevel(PetHungryLevel level) {
		hungryLevel = level;
	}

	/**
	 * @return the consumed
	 */
	public int getRegularCount() {
		return regularConsumed & 0xFF;
	}

	public void setRegularCount(short count) {
		regularConsumed = count;
	}

	public int getLovedFoodRemaining() {
		return lovedFoodMax - lovedConsumed;
	}

	public boolean isLovedFeeded() {
		return lovedFeeded;
	}

	public void setIsLovedFeeded() {
		lovedFeeded = true;
	}

	public void incrementCount(boolean lovedFood) {
		if (lovedFood) {
			lovedConsumed++;
		} else {
			regularConsumed++;
		}
	}

	public void reset() {
		if (lovedFeeded)
			lovedFeeded = false;
		else {
			totalPoints = 0;
			regularConsumed = 0;
		}
	}

	public int getDataForPacket() {
		int value = getRegularCount() & 0xFF;
		value <<= 14;
		value |= totalPoints >> 2;
		value <<= 6;
		value |= lovedConsumed & 0x3F;
		value <<= 4; // unk
		return value;
	}

	public void setData(int savedData) {
		savedData >>= 4; // drop unk
		lovedConsumed = (short) (savedData & 0x3F);
		savedData >>= 6;
		totalPoints = (savedData & 0x3FFF) << 2;
		savedData >>= 14;
		regularConsumed = (short) (savedData & 0xFF);
	}
}
