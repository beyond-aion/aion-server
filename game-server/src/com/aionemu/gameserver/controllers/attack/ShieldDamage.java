package com.aionemu.gameserver.controllers.attack;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.model.Effect;

/**
 * Damage which a shield redirects to another creature: reflected back to the attacker or taken over by a protector. It is calculated together
 * with the attack but dealt when the attack hits, reflected damage right before the hit and protected damage right after it.
 *
 * @param receiver
 *          creature which receives the damage
 * @param attacker
 *          creature the damage is credited to
 * @param effect
 *          effect the damage is dealt with
 * @param type
 *          type of the damage
 * @param damage
 *          damage
 * @param attackStatus
 *          attack status shown with the damage, null for none
 * @param beforeHit
 *          whether the damage is dealt before the hit itself
 * @param lethalHitLatched
 *          whether the receiver was latched as doomed by this damage and must be released once it is dealt
 */
public record ShieldDamage(Creature receiver, Creature attacker, Effect effect, TYPE type, int damage, AttackStatus attackStatus, boolean beforeHit,
	boolean lethalHitLatched) {

	/**
	 * @return Damage which the reflector shield effect sends back to the attacker, latching the attacker as doomed if it is lethal
	 */
	public static ShieldDamage reflected(Creature attacker, Effect reflectorEffect, int damage) {
		Creature reflector = reflectorEffect.getEffected();
		boolean latched = attacker.getLifeStats().latchLethalHit(reflector, damage);
		return new ShieldDamage(attacker, reflector, reflectorEffect, TYPE.REGULAR, damage, null, true, latched);
	}

	/**
	 * @return Damage which the protector takes over from the protected creature
	 */
	public static ShieldDamage protectedDamage(Creature protector, Creature attacker, Effect attackerEffect, int damage, AttackStatus attackStatus) {
		return new ShieldDamage(protector, attacker, attackerEffect, TYPE.PROTECTDMG, damage, attackStatus, false, false);
	}

	public static void applyAll(List<ShieldDamage> shieldDamages, boolean beforeHit) {
		for (ShieldDamage shieldDamage : shieldDamages)
			if (shieldDamage.beforeHit == beforeHit)
				shieldDamage.apply();
	}

	public void apply() {
		try {
			receiver.getController().onAttack(attacker, effect, type, damage, false, LOG.REGULAR, attackStatus, null);
		} finally {
			discard();
		}
	}

	/**
	 * Releases the receiver's latch without dealing the damage, for damage of an attack which will never hit.
	 */
	public void discard() {
		if (lethalHitLatched)
			receiver.getLifeStats().releaseLethalHit();
	}
}
