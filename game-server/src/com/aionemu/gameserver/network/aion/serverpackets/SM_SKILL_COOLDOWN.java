package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.*;
import java.util.stream.Collectors;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.PlayerSkillEntry;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ATracer, nrg
 */
public class SM_SKILL_COOLDOWN extends AionServerPacket {

	private final List<Cooldown> cooldowns = new ArrayList<>();
	private final boolean notify;

	public SM_SKILL_COOLDOWN(int skillId, long expirationTimeMillis) {
		cooldowns.add(new Cooldown(skillId, expirationTimeMillis));
		notify = true;
	}

	public SM_SKILL_COOLDOWN(Player player, Map<Integer, Long> cooldownExpirationMillisByCooldownId, boolean notify) {
		for (PlayerSkillEntry skill : player.getSkillList().getAllSkills()) {
			int cooldownId = DataManager.SKILL_DATA.getSkillTemplate(skill.getSkillId()).getCooldownId();
			Long cooldownExpirationMillis = cooldownExpirationMillisByCooldownId.get(cooldownId);
			if (cooldownExpirationMillis != null)
				cooldowns.add(new Cooldown(skill.getSkillId(), cooldownExpirationMillis));
		}
		// The game plays the same icon cooldown animation for all skills that share a cooldownId and the last entry per cooldownId wins, so we sort by
		// animation duration to avoid animations that are shorter than the remaining cooldown time.
		cooldowns.sort(Comparator.comparingInt(Cooldown::getDurationMillis));
		this.notify = notify;
	}

	/**
	 * Creates a skill cooldown reset packet
	 */
	public SM_SKILL_COOLDOWN(Player player, Collection<Integer> resettableCooldownIds) {
		this(player, resettableCooldownIds.stream().collect(Collectors.toMap(c -> c, _ -> 0L)), true);
	}

	@Override
	protected void writeImpl(AionConnection con) {
		writeH(cooldowns.size());
		writeC(notify ? 1 : 0); // 1 will trigger a notification sound and animation on all sent skills
		for (Cooldown cooldown : cooldowns) {
			writeH(cooldown.skillId);
			writeD(cooldown.getRemainingSeconds());
			writeD(cooldown.getDurationMillis()); // 0 also seems to always work
		}
	}

	private record Cooldown(int skillId, long expirationTimeMillis) {

		int getRemainingSeconds() {
			return expirationTimeMillis == 0 ? 0 : (int) Math.max(0, (expirationTimeMillis - System.currentTimeMillis()) / 1000);
		}

		int getDurationMillis() {
			return DataManager.SKILL_DATA.getSkillTemplate(skillId).getCooldown() * 100;
		}
	}
}
