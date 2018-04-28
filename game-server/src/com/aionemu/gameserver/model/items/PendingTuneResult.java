package com.aionemu.gameserver.model.items;

/**
 * @author Estrayl
 */
public class PendingTuneResult {

	private final int optionalSockets;
	private final int enchantBonus;
	private final int rndBonusSetId;
	private final boolean shouldNotReduceTuneCount;

	public PendingTuneResult(int optionalSockets, int enchantBonus, int rndBonusSetId, boolean shouldNotReduceTuneCount) {
		this.optionalSockets = optionalSockets;
		this.enchantBonus = enchantBonus;
		this.rndBonusSetId = rndBonusSetId;
		this.shouldNotReduceTuneCount = shouldNotReduceTuneCount;
	}

	public int getOptionalSockets() {
		return optionalSockets;
	}

	public int getEnchantBonus() {
		return enchantBonus;
	}

	public int getBonusSetId() {
		return rndBonusSetId;
	}

	public boolean shouldNotReduceTuneCount() {
		return shouldNotReduceTuneCount;
	}

}
