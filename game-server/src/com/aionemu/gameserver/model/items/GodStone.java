package com.aionemu.gameserver.model.items;

import java.util.concurrent.atomic.AtomicLong;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;

/**
 * @author ATracer
 */
public class GodStone extends ItemStone {

	private final AtomicLong cooldownExpireTimeMillis = new AtomicLong();
	private final GodstoneInfo godstoneInfo;
	private int activatedCount;

	public GodStone(Item parentItem, int activatedCount, int itemId, GodstoneInfo godstoneInfo, PersistentState state) {
		super(parentItem.getObjectId(), itemId, 0, state);
		this.godstoneInfo = godstoneInfo;
		this.activatedCount = activatedCount;
	}

	public GodstoneInfo getGodstoneInfo() {
		return godstoneInfo;
	}

	public void increaseActivatedCount() {
		activatedCount++;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getActivatedCount() {
		return activatedCount;
	}

	public boolean tryActivate(boolean isMainHandWeapon, Creature target) {
		long now = System.currentTimeMillis();
		if (CustomConfig.GODSTONE_ACTIVATION_RATE <= 0 || now < cooldownExpireTimeMillis.get())
			return false;
		cooldownExpireTimeMillis.set(now + CustomConfig.GODSTONE_EVALUATION_COOLDOWN_MILLIS);

		int procProbability = isMainHandWeapon ? godstoneInfo.getProbability() : godstoneInfo.getProbabilityLeft();
		procProbability -= target.getGameStats().getStat(StatEnum.PROC_REDUCE_RATE, 0).getCurrent();
		if (procProbability > 0)
			procProbability = Math.max(1, Math.round(procProbability * CustomConfig.GODSTONE_ACTIVATION_RATE));

		return Rnd.get(1, 1000) <= procProbability;
	}
}
