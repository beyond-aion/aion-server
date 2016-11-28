package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.List;
import java.util.Map;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer, nrg
 */
public class SM_SKILL_COOLDOWN extends AionServerPacket {

	private Map<Integer, Long> cooldowns;

	private boolean onLogin;

	public SM_SKILL_COOLDOWN(Map<Integer, Long> cooldowns, boolean onLogin) {
		this.cooldowns = cooldowns;
		this.onLogin = onLogin;
	}

	public SM_SKILL_COOLDOWN(Map<Integer, Long> cooldowns) {
		this(cooldowns, false);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(calculateSize());
		writeC(1); // unk 0 or 1
		long currentTime = System.currentTimeMillis();
		for (Map.Entry<Integer, Long> entry : cooldowns.entrySet()) {
			int left = (int) ((entry.getValue() - currentTime) / 1000);
			List<Integer> skillsWithCooldown = DataManager.SKILL_DATA.getSkillsForCooldownId(entry.getKey());

			for (int index = 0; index < skillsWithCooldown.size(); index++) {
				int skillId = skillsWithCooldown.get(index);
				writeH(skillId);
				writeD(left > 0 ? left : 0);
				writeD(onLogin ? 0 : DataManager.SKILL_DATA.getSkillTemplate(skillId).getCooldown());
			}
		}
	}

	private int calculateSize() {
		int size = 0;
		for (Map.Entry<Integer, Long> entry : cooldowns.entrySet()) {
			size += DataManager.SKILL_DATA.getSkillsForCooldownId(entry.getKey()).size();
		}
		return size;
	}

}
