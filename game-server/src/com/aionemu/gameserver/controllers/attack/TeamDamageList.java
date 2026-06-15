package com.aionemu.gameserver.controllers.attack;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.AionObject;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;

/**
 * List of combined creature damages, grouped by the team they belong to (if present).
 */
public class TeamDamageList {

	private final Map<AionObject, DamageInfo<AionObject>> damageByCreatureOrTeam = new HashMap<>();
	private final Map<TemporaryPlayerTeam<?>, DamageInfo<Player>> mostDamageByTeam = new HashMap<>();

	@SuppressWarnings("unchecked")
	TeamDamageList(DamageList damageList) {
		for (DamageInfo<? extends Creature> damageInfo : damageList.getCreatureDamages()) {
			AionObject creatureOrTeam = damageInfo.getAttacker();
			TemporaryPlayerTeam<?> team = creatureOrTeam instanceof Player player ? player.getCurrentTeam() : null;
			if (team != null) {
				creatureOrTeam = team;
				DamageInfo<Player> memberDamage = (DamageInfo<Player>) damageInfo;
				mostDamageByTeam.compute(team, (_, other) -> other == null || memberDamage.getDamage() > other.getDamage() ? memberDamage : other);
			}
			damageByCreatureOrTeam.computeIfAbsent(creatureOrTeam, DamageInfo::new).addDamage(damageInfo.getDamage());
		}
	}

	public Collection<DamageInfo<AionObject>> getCreatureOrTeamDamages() {
		return damageByCreatureOrTeam.values();
	}

	public DamageInfo<AionObject> getMostDamage() {
		return damageByCreatureOrTeam.values().stream().max(Comparator.comparingInt(DamageInfo::getDamage)).orElse(null);
	}

	public DamageInfo<Player> getMostDamageByTeam(TemporaryPlayerTeam<?> team) {
		return mostDamageByTeam.get(team);
	}

	public int getTotalDamage() {
		return damageByCreatureOrTeam.values().stream().mapToInt(DamageInfo::getDamage).sum();
	}
}
