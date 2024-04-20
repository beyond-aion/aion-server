package com.aionemu.gameserver.skillengine.model;

/**
 * @author kecimis, Neon
 */
public class ChainSkills {

	private ChainSkill previousChainSkill = new ChainSkill("");
	private ChainSkill chainSkill = new ChainSkill("");
	private long expireTime = 0;

	/**
	 * @return The chain skill used before the current one.
	 */
	public ChainSkill getPreviousChainSkill() {
		return previousChainSkill;
	}

	/**
	 * @return The last used chain skill.
	 */
	public ChainSkill getCurrentChainSkill() {
		return chainSkill;
	}

	/**
	 * @return Number of activations for the current chain skill. 0 if chain skill category doesn't match the current one, or no chain is active.
	 */
	public int getCurrentChainCount(String category) {
		return chainSkill.getCategory().equals(category) ? chainSkill.getUseCount() : 0;
	}

	public void updateChain(String category, int duration) {
		if (chainSkill.getCategory().isEmpty())
			chainSkill.setCategory(category);
		else if (!chainSkill.getCategory().equals(category)) {
			previousChainSkill = chainSkill;
			chainSkill = new ChainSkill(category);
		}

		chainSkill.increaseUseCount();
		expireTime = duration == 0 ? 0 : System.currentTimeMillis() + duration;
	}

	/**
	 * Resets the complete chain (clears all info).
	 */
	public void resetChain() {
		if (!chainSkill.getCategory().isEmpty()) {
			expireTime = 0;
			previousChainSkill.clear();
			chainSkill.clear();
		}
	}

	/**
	 * @return True if this chain is expired. It must be reset to make it usable again.
	 */
	public boolean isChainExpired() {
		return expireTime > 0 && System.currentTimeMillis() > expireTime;
	}
}
