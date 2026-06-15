package com.aionemu.gameserver.controllers.observer;

import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.LOG;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ATTACK_STATUS.TYPE;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.model.*;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author ATracer, Sippolo, kecimis, Luzien, Neon
 */
public class AttackShieldObserver extends AttackCalcObserver {

	private final Effect effect;
	private final HitType hitType;
	private final ShieldType shieldType;
	private final int hit;
	private final boolean hitPercent;
	private int totalHit;
	private final boolean totalHitPercent;
	private final int probability;
	private final int minRadius;
	private final int maxRadius;
	private final HealType healType;
	private final int mpValue;

	private boolean totalHitPercentSet = false;

	public AttackShieldObserver(int hit, int totalHit, boolean percent, Effect effect, HitType type, ShieldType shieldType, int probability) {
		this(hit, totalHit, percent, false, effect, type, shieldType, probability, 0, 100, null, 0);
	}

	public AttackShieldObserver(int hit, int totalHit, boolean percent, Effect effect, HitType type, ShieldType shieldType, int probability,
		int mpValue) {
		this(hit, totalHit, percent, false, effect, type, shieldType, probability, 0, 100, null, mpValue);
	}

	public AttackShieldObserver(int hit, int totalHit, boolean hitPercent, boolean totalHitPercent, Effect effect, HitType type, ShieldType shieldType,
		int probability, int minRadius, int maxRadius, HealType healType, int mpValue) {
		this.hit = hit;
		this.totalHit = totalHit; // total absorbed dmg for shield, percentage for reflector, received dmg percentage for protect
		this.effect = effect;
		this.hitPercent = hitPercent;
		this.totalHitPercent = totalHitPercent;
		this.hitType = type;
		this.shieldType = shieldType;
		this.probability = probability;
		this.minRadius = minRadius; // only for reflector
		this.maxRadius = maxRadius; // for reflector / protect
		this.healType = healType; // only for ConvertHeal
		this.mpValue = mpValue;
	}

	@Override
	public void checkShield(List<AttackResult> attackList, Effect attackerEffect, Creature attacker) {
		for (AttackResult attackResult : attackList) {
			AttackStatus baseStatus = AttackStatus.getBaseStatus(attackResult.getAttackStatus());
			if (baseStatus == AttackStatus.DODGE || baseStatus == AttackStatus.RESIST)
				continue;

			// Handle Hit Types for Shields
			switch (hitType) {
				case EVERYHIT:
					break;
				case SKILL:
					if (attackerEffect == null)
						continue;
					break;
				default:
					if (attackResult.getHitType() != null && hitType != attackResult.getHitType())
						continue;
			}

			if (probability < 100 && Rnd.chance() >= probability)
				continue;

			// shield type 2 or 16, normal shield, MP
			if (shieldType == ShieldType.NORMAL || shieldType == ShieldType.MPSHIELD) {
				int damage = attackResult.getDamage();

				int absorbedDamage;
				if (hitPercent)
					absorbedDamage = damage * hit / 100;
				else
					absorbedDamage = Math.min(damage, hit);

				absorbedDamage = Math.min(absorbedDamage, totalHit);
				totalHit -= absorbedDamage;

				if (absorbedDamage > 0)
					attackResult.setShieldType(shieldType.getId());
				attackResult.setDamage(damage - absorbedDamage);

				// don't launch sub effect if damage is fully absorbed
				if (absorbedDamage >= damage && !isPunchShield(attackerEffect))
					attackResult.setLaunchSubEffect(false);

				if (mpValue != 0) {
					int mp = (int) (absorbedDamage * 0.01f * mpValue);
					// TODO recheck sm_attack_status
					effect.getEffected().getLifeStats().reduceMp(SM_ATTACK_STATUS.TYPE.USED_MP, mp, 0, SM_ATTACK_STATUS.LOG.REGULAR);
					attackResult.setMpAbsorbed(mp);
					attackResult.setMpShieldSkillId(effect.getSkillId());
				}

				if (totalHit <= 0) {
					effect.endEffect();
					return;
				}
			} else if (shieldType == ShieldType.REFLECTOR || shieldType == ShieldType.SKILL_REFLECTOR) { // shield type 1, reflected damage
				if (minRadius != 0) {
					if (PositionUtil.isInRange(attacker, effect.getEffected(), minRadius, false))
						continue;
				}
				if (PositionUtil.isInRange(attacker, effect.getEffected(), maxRadius, false)) {
					int reflectedHit = attackResult.getDamage();
					if (hit > 0 || totalHit > 0) {
						int reflectedDamage = attackResult.getDamage() * totalHit / 100;
						reflectedHit = Math.max(reflectedDamage, hit); // percentage of damage, but at least hit value
					}
					attackResult.setShieldType(shieldType.getId());
					if (attacker instanceof Npc) {
						reflectedHit = (int) attacker.getAi().modifyDamage(attacker, reflectedHit, effect);
					}
					attackResult.setReflectedDamage(reflectedHit);
					attackResult.setReflectedSkillId(effect.getSkillId());

					if (shieldType == ShieldType.SKILL_REFLECTOR) { // whole skill reflections are applied implicitly, see Effect#getEffected()
						attackerEffect.setForceType(ForceType.DEFAULT); // make sure it hits the effector (no checks needed at this point)
						effect.endEffect(); // one skill reflection ends the shield effect
						return;
					} else { // apply reflect damage
						attacker.getController().onAttack(effect.getEffected(), effect, TYPE.REGULAR, reflectedHit, false, LOG.REGULAR, null, null);
					}
				}
				break;
			} else if (shieldType == ShieldType.PROTECT) { // shield type 8, protect effect (ex. skillId: 417 Bodyguard I)
				if (effect.getEffector() == null || effect.getEffector().isDead()) {
					effect.endEffect();
					break;
				}
				if (effect.getEffector() instanceof Summon
					&& (((Summon) effect.getEffector()).getMode() == SummonMode.RELEASE || ((Summon) effect.getEffector()).getMaster() == null)) {
					effect.endEffect();
					break;
				}

				if (PositionUtil.isInRange(effect.getEffector(), effect.getEffected(), maxRadius, false)) {
					int damageProtected = 0;
					int effectorDamage = 0;

					if (hitPercent) {
						damageProtected = (int) (attackResult.getDamage() * hit * 0.01);
						if (totalHit > 0) // reduce the effectively received damage (totalHit = percent of received dmg)
							effectorDamage = attackResult.getDamage() * totalHit / 100;
						else
							effectorDamage = attackResult.getDamage();
					} else
						damageProtected = hit;
					int finalDamage = Math.max(0, attackResult.getDamage() - damageProtected);
					attackResult.setDamage(finalDamage);
					attackResult.setShieldType(shieldType.getId());
					attackResult.setProtectedSkillId(effect.getSkillId());
					attackResult.setProtectedDamage(effectorDamage);
					attackResult.setProtectorId(effect.getEffectorId());
					effect.getEffector().getController().onAttack(attacker, attackerEffect, TYPE.PROTECTDMG, effectorDamage, false, LOG.REGULAR,
						attackResult.getAttackStatus(), null);
					// dont launch subeffect if damage is fully absorbed
					if (!isPunchShield(attackerEffect))
						attackResult.setLaunchSubEffect(false);
				}
			} else if (shieldType == ShieldType.CONVERT) { // shield type 0, convertHeal
				int damage = attackResult.getDamage();

				int absorbedDamage = damage;

				if (totalHitPercent && !totalHitPercentSet) {
					totalHit = (int) (totalHit * 0.01 * effect.getEffected().getGameStats().getHealth().getCurrent());
					totalHitPercentSet = true;
				}

				absorbedDamage = Math.min(absorbedDamage, totalHit);
				totalHit -= absorbedDamage;

				attackResult.setDamage(damage - absorbedDamage);

				// heal part
				int healValue = 0;
				if (hitPercent)
					healValue = damage * hit / 100;
				else
					healValue = hit;

				switch (healType) {
					case HP:
						effect.getEffected().getLifeStats().increaseHp(TYPE.HP, healValue, effect, LOG.REGULAR);
						break;
					case MP:
						effect.getEffected().getLifeStats().increaseMp(TYPE.HEAL_MP, healValue, effect.getSkillId(), LOG.REGULAR);
						break;
				}

				// dont launch subeffect if damage is fully absorbed
				if (absorbedDamage >= damage && !isPunchShield(attackerEffect))
					attackResult.setLaunchSubEffect(false);

				if (totalHit <= 0) {
					effect.endEffect();
					return;
				}
			}
		}
	}

	private boolean isPunchShield(Effect effect) {
		if (effect == null)
			return false;
		for (EffectTemplate template : effect.getEffectTemplates()) {
			if (template.getSubEffect() != null) {
				SkillTemplate skill = DataManager.SKILL_DATA.getSkillTemplate(template.getSubEffect().getSkillId());
				if (skill.isProvoked())
					return true;
			}
		}
		return false;
	}

	public ShieldType getShieldType() {
		return shieldType;
	}

}
