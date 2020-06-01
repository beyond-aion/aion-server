package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.player.Cooldowns;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * zzsort, Sykra
 */
public class SM_RECIPE_COOLDOWN extends AionServerPacket {

	/**
	 * 0 - unknown.
	 * 1 - update recipe cooldown times.
	 */
	private int mode = 0;
	private final Map<Integer, Integer> cooldowns = new HashMap<>();

	public SM_RECIPE_COOLDOWN(Player player, int mode) {
		this.mode = mode;
		Cooldowns craftCooldowns = player.getCraftCooldowns();
		if (!craftCooldowns.isEmpty())
			craftCooldowns.forEach((cooldownId, value) -> cooldowns.put(cooldownId, craftCooldowns.remainingSeconds(cooldownId)));
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeC(mode);
		writeH(cooldowns.size());
		cooldowns.forEach((cooldownId, remainingSeconds) -> {
			writeD(cooldownId);
			writeD(remainingSeconds);
		});
	}

}
