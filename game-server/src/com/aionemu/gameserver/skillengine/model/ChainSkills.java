package com.aionemu.gameserver.skillengine.model;

/**
 * The chain state of a player: the category of the last counted skill, how often it was counted in a row and the time its hit landed. Each chain
 * skill checks its own activation window against this time.
 *
 * @author kecimis, Neon
 */
public class ChainSkills {

	private String category = "";
	private int useCount;
	private long hitTimeMillis;

	/**
	 * @return Number of consecutive activations for the given chain category. 0 if the current chain has another category, or no chain is active.
	 */
	public int getCurrentChainCount(String category) {
		return this.category.equals(category) ? useCount : 0;
	}

	public boolean isCurrentCategory(String category) {
		return this.category.equals(category);
	}

	/**
	 * @return True if a skill of the current category may be activated once more within its own activation window.
	 */
	public boolean canRepeat(String category, int allowedActivations, int windowMillis) {
		return this.category.equals(category) && useCount < allowedActivations && isWithinWindow(windowMillis);
	}

	/**
	 * @return True if the current chain is the required predecessor and the follow-up skill is still within its own activation window.
	 */
	public boolean canContinue(String preCategory, int preCount, int windowMillis) {
		return category.equals(preCategory) && useCount >= preCount && isWithinWindow(windowMillis);
	}

	private boolean isWithinWindow(int windowMillis) {
		return System.currentTimeMillis() < hitTimeMillis + windowMillis;
	}

	/**
	 * Counts a used skill. A skill without chain category passes an empty one, which ends the current chain.
	 */
	public void updateChain(String category, int hitTime) {
		if (this.category.equals(category)) {
			useCount++;
		} else {
			this.category = category;
			useCount = 1;
		}
		hitTimeMillis = System.currentTimeMillis() + hitTime;
	}

	/**
	 * Resets the complete chain (clears all info).
	 */
	public void resetChain() {
		category = "";
		useCount = 0;
		hitTimeMillis = 0;
	}
}
