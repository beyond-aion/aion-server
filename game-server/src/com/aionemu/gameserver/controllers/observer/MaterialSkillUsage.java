package com.aionemu.gameserver.controllers.observer;

import java.util.HashMap;
import java.util.Map;

/**
 * Last uses of material skills, kept per creature so that stepping off a material and back on (or re-entering a material zone) does not restart
 * the skill periods. Slots are the positions of the skills in their material template.
 */
public class MaterialSkillUsage {

	private final Map<Integer, Long> lastUseMillisBySlot = new HashMap<>();
	private int lastMaterialId;

	/**
	 * @return True if the skill in the given slot may be used now, in which case the use is recorded. A slot is blocked only while its period runs
	 *         and the last used material is the same one.
	 */
	public synchronized boolean tryUse(int materialId, int slot, int periodSeconds) {
		long now = System.currentTimeMillis();
		Long lastUseMillis = lastUseMillisBySlot.get(slot);
		if (lastUseMillis != null && materialId == lastMaterialId && now < lastUseMillis + periodSeconds * 1000L)
			return false;
		lastMaterialId = materialId;
		lastUseMillisBySlot.put(slot, now);
		return true;
	}
}
