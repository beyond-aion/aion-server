package com.aionemu.gameserver.controllers.attack;

import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.Creature;

/**
 * List of combined creature damages, grouped by their master (if present, like with Summons and summoned objects).
 */
public class DamageList {

	private final Map<Creature, DamageInfo<Creature>> damageByCreature = new HashMap<>();

	DamageList(Collection<AggroInfo> aggroInfos, Creature owner) {
		for (AggroInfo aggroInfo : aggroInfos) {
			if (aggroInfo.getDamage() <= 0)
				continue;
			Creature attackerMaster = aggroInfo.getAttacker().getMaster();
			// Don't include damage from creatures outside the known list.
			if (!owner.getKnownList().knows(attackerMaster))
				continue;
			damageByCreature.computeIfAbsent(attackerMaster, DamageInfo::new).addDamage(aggroInfo.getDamage());
		}
	}

	public TeamDamageList toTeamDamages() {
		return new TeamDamageList(this);
	}

	public Collection<DamageInfo<Creature>> getCreatureDamages() {
		return damageByCreature.values();
	}

	public DamageInfo<Creature> getMostDamage() {
		return damageByCreature.values().stream().max(Comparator.comparingInt(DamageInfo::getDamage)).orElse(null);
	}

	public int getTotalDamage() {
		return damageByCreature.values().stream().mapToInt(DamageInfo::getDamage).sum();
	}

}
