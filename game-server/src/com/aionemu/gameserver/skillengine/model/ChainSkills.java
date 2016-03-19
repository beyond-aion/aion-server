package com.aionemu.gameserver.skillengine.model;

/**
 * @author kecimis
 * @modified Neon
 */
public class ChainSkills {

	private ChainSkill previousChainSkill = new ChainSkill("", 0, 0);
	private ChainSkill chainSkill = new ChainSkill("", 0, 0);

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
		if (chainSkill.getCategory().equals("")) {
			chainSkill.setCategory(category);
			chainSkill.setExpireTime(duration == 0 ? 0 : System.currentTimeMillis() + duration);
		}

		if (chainSkill.getCategory().equals(category)) {
			chainSkill.increaseUseCount();
		} else {
			previousChainSkill = chainSkill;
			chainSkill = new ChainSkill(category, 1, duration == 0 ? 0 : System.currentTimeMillis() + duration);
		}
	}

	public void resetChain() {
		if (chainSkill.getUseCount() > 0) {
			previousChainSkill.clear();
			chainSkill.clear();
		}
	}
}
