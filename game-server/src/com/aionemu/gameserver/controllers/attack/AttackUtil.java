package com.aionemu.gameserver.controllers.attack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Servant;
import com.aionemu.gameserver.model.gameobjects.Trap;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.NpcEquippedGear;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.aionemu.gameserver.skillengine.change.Func;
import com.aionemu.gameserver.skillengine.effect.DamageEffect;
import com.aionemu.gameserver.skillengine.effect.DelayedSpellAttackInstantEffect;
import com.aionemu.gameserver.skillengine.effect.DispelBuffCounterAtkEffect;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.NoReduceSpellATKInstantEffect;
import com.aionemu.gameserver.skillengine.effect.ProcAtkInstantEffect;
import com.aionemu.gameserver.skillengine.effect.SkillAttackInstantEffect;
import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifier;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;
import com.aionemu.gameserver.skillengine.model.HitType;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author ATracer
 */
public class AttackUtil {

	/**
	 * Calculate physical attack status and damage
	 */
	public static List<AttackResult> calculatePhysicalAttackResult(Creature attacker, Creature attacked) {
		AttackStatus attackerStatus = null;
		int damage = StatFunctions.calculateAttackDamage(attacker, attacked, true, SkillElement.NONE);
		List<AttackResult> attackList = new ArrayList<>();
		AttackStatus mainHandStatus = calculateMainHandResult(attacker, attacked, attackerStatus, damage, attackList, SkillElement.NONE);

		if (attacker instanceof Player && ((Player) attacker).getEquipment().getOffHandWeaponType() != null) {
			calculateOffHandResult(attacker, attacked, mainHandStatus, attackList, SkillElement.NONE);
		}
		attacked.getObserveController().checkShieldStatus(attackList, null, attacker);
		return attackList;
	}

	/**
	 * Calculate physical attack status and damage of the MAIN hand
	 */
	private static AttackStatus calculateMainHandResult(Creature attacker, Creature attacked, AttackStatus attackerStatus, int damage,
		List<AttackResult> attackList, SkillElement elem) {
		AttackStatus mainHandStatus = attackerStatus;
		Item mainHandWeapon;
		int mainHandHits = 1;
		switch (elem) {
			case NONE:
				if (mainHandStatus == null)
					mainHandStatus = calculatePhysicalStatus(attacker, attacked, true);
				if (attacker instanceof Player) {
					mainHandWeapon = ((Player) attacker).getEquipment().getMainHandWeapon();
					if (mainHandWeapon != null)
						mainHandHits = Rnd.get(1, mainHandWeapon.getItemTemplate().getWeaponStats().getHitCount());
				}
				splitPhysicalDamage(attacker, attacked, mainHandHits, damage, mainHandStatus, attackList, elem, true);
				break;
			default: {
				if (mainHandStatus == null)
					mainHandStatus = calculateMagicalStatus(attacker, attacked, 100, false);
				if (attacker instanceof Player) {
					mainHandWeapon = ((Player) attacker).getEquipment().getMainHandWeapon();
					if (mainHandWeapon != null)
						mainHandHits = Rnd.get(1, mainHandWeapon.getItemTemplate().getWeaponStats().getHitCount());
				}
				splitMagicalDamage(attacker, attacked, mainHandHits, damage, mainHandStatus, attackList, elem, true);
				break;
			}
		}
		return mainHandStatus;
	}

	/**
	 * Calculate physical attack status and damage of the OFF hand
	 */
	private static void calculateOffHandResult(Creature attacker, Creature attacked, AttackStatus mainHandStatus, List<AttackResult> attackList,
		SkillElement element) {
		AttackStatus offHandStatus = AttackStatus.getOffHandStats(mainHandStatus);
		Item offHandWeapon = ((Player) attacker).getEquipment().getOffHandWeapon();
		int offHandDamage = StatFunctions.calculateAttackDamage(attacker, attacked, false, element);
		int offHandHits = Rnd.get(1, offHandWeapon.getItemTemplate().getWeaponStats().getHitCount());
		switch (element) {
			case NONE:
				splitPhysicalDamage(attacker, attacked, offHandHits, offHandDamage, offHandStatus, attackList, element, false);
				break;
			default:
				splitMagicalDamage(attacker, attacked, offHandHits, offHandDamage, offHandStatus, attackList, element, false);
				break;
		}
	}

	/**
	 * Generate attack results based on weapon hit count
	 */
	private static List<AttackResult> splitPhysicalDamage(Creature attacker, Creature attacked, int hitCount, int damage, AttackStatus status,
		List<AttackResult> attackList, SkillElement element, boolean isMain) {

		switch (AttackStatus.getBaseStatus(status)) {
			case BLOCK:
				damage = calculateBlockedDamage(attacked, damage);
				break;
			case DODGE:
				damage = 0;
				break;
			case PARRY:
				damage *= 0.6;
				break;
		}

		if (status.isCritical())
			damage = (int) calculateWeaponCritical(element, attacked, damage, getWeaponGroup(attacker, isMain), StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE,
				isMain);

		if (damage < 1)
			damage = 0;

		if (hitCount == 1) {
			attackList.add(new AttackResult(damage, status, HitType.PHHIT));
		} else {
			// example: 2385 total dmg with hitCount 4 is 1x 1836 (mainHit) + 3x 183 (minorHits), or 1x 2169 + 1x 216 (hitCount 2)
			int minorHits = damage / (hitCount + 9);
			int mainHit = damage - (minorHits * (hitCount - 1));
			attackList.add(new AttackResult(mainHit, status, HitType.PHHIT));
			for (int i = 1; i < hitCount; i++)
				attackList.add(new AttackResult(minorHits, AttackStatus.NORMALHIT, HitType.PHHIT));
		}
		return attackList;
	}

	private static int calculateBlockedDamage(Creature attacked, int damage) {
		int reduceStat = 50 + attacked.getGameStats().getStat(StatEnum.DAMAGE_REDUCE, 0).getCurrent();
		int reduceVal = (int) ((damage * 0.01f) * reduceStat);
		if (attacked instanceof Player) {
			Item shield = ((Player) attacked).getEquipment().getEquippedShield();
			if (shield != null) {
				int reduceMax = shield.getItemTemplate().getWeaponStats().getReduceMax();
				if (reduceMax > 0 && reduceMax < reduceVal)
					reduceVal = reduceMax;
			}
		}
		return damage - reduceVal;
	}

	private static List<AttackResult> splitMagicalDamage(Creature attacker, Creature attacked, int hitCount, int damage, AttackStatus status,
		List<AttackResult> attackList, SkillElement element, boolean isMain) {

		switch (AttackStatus.getBaseStatus(status)) {
			case RESIST:
				damage = 0;
				break;
		}

		if (status.isCritical())
			damage = (int) calculateWeaponCritical(element, attacked, damage, getWeaponGroup(attacker, isMain), StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE,
				isMain);

		if (damage < 1)
			damage = 0;

		if (hitCount == 1) {
			attackList.add(new AttackResult(damage, status, HitType.MAHIT));
		} else {
			int minorHits = damage / (hitCount + 9);
			int mainHit = damage - (minorHits * (hitCount - 1));
			attackList.add(new AttackResult(mainHit, status, HitType.MAHIT));
			for (int i = 1; i < hitCount; i++)
				attackList.add(new AttackResult(minorHits, AttackStatus.NORMALHIT, HitType.MAHIT));
		}
		return attackList;
	}

	/**
	 * @param element
	 * @param attacked
	 * @param damages
	 * @param group
	 * @param stat
	 * @param isMain
	 * @return critical damage
	 */
	private static float calculateWeaponCritical(SkillElement element, Creature attacked, float damages, ItemGroup group, StatEnum stat, boolean isMain) {
		return calculateWeaponCritical(element, attacked, damages, group, 0, stat, isMain);
	}

	/**
	 * @param element
	 * @param attacked
	 * @param damages
	 * @param group
	 * @param critAddDmg
	 * @param stat
	 * @param isMain
	 * @return critical damage
	 */
	private static float calculateWeaponCritical(SkillElement element, Creature attacked, float damages, ItemGroup group, int critAddDmg, StatEnum stat, boolean isMain) {
		float coeficient = 1.5f;
		if (element == SkillElement.NONE && group != null) {
			coeficient = getWeaponMultiplier(group);
		}

		if (stat != null && attacked instanceof Player) { // Strike Fortitude lowers the crit multiplier
			switch (stat) {
				case PHYSICAL_CRITICAL_DAMAGE_REDUCE:
				case MAGICAL_CRITICAL_DAMAGE_REDUCE:
					int fortitude = attacked.getGameStats().getStat(stat, 0).getCurrent();
					coeficient = isMain ? (coeficient - fortitude / 1000f) : (coeficient + fortitude / 1000f);
					break;
			}
		}

		// add critical add dmg
		coeficient += critAddDmg / 100f;
		damages = Math.round(damages * coeficient);
		return damages;
	}

	private static float getWeaponMultiplier(ItemGroup group) {
		switch (group) {
			case DAGGER:
				return 2.3f;
			case SWORD:
				return 2.2f;
			case MACE:
				return 2f;
			case GREATSWORD:
			case POLEARM:
				return 1.8f;
			case STAFF:
			case BOW:
				return 1.7f;
			default:
				return 1.5f;
		}
	}

	/**
	 * @param effect
	 * @param skillDamage
	 * @param template
	 * @param ignoreShield
	 */
	public static void calculateSkillResult(Effect effect, int skillDamage, EffectTemplate template, boolean ignoreShield) {
		Creature effector = effect.getEffector();
		Creature effected = effect.getEffected();
		// define values
		ActionModifier modifier = template.getActionModifiers(effect);
		SkillElement element = template.getElement();
		Func func = (template instanceof DamageEffect ? ((DamageEffect) template).getMode() : Func.ADD);
		int randomDamageType = (template instanceof SkillAttackInstantEffect ? ((SkillAttackInstantEffect) template).getRnddmg() : 0);
		int skillLvl = effect.getSkillLevel();
		int accMod = template.getAccMod2() + template.getAccMod1() * skillLvl;
		int criticalProb = template.getCritProbMod2();
		if (template instanceof DispelBuffCounterAtkEffect)
			criticalProb = 0;// critprob 0, dispelbuffcounteratkeffect can not crit
		int critAddDmg = template.getCritAddDmg2() + template.getCritAddDmg1() * skillLvl;
		boolean cannotMiss = template instanceof SkillAttackInstantEffect && ((SkillAttackInstantEffect) template).isCannotmiss();
		boolean shared = template instanceof DamageEffect && ((DamageEffect) template).isShared();
		boolean useTemplateDmg = isUseTemplateDmg(effect, template);
		// for sm_castspell_result packet
		int position = template.getPosition();
		boolean send = true;
		if (template instanceof DelayedSpellAttackInstantEffect || template instanceof ProcAtkInstantEffect)
			send = false;

		int damage = 0;
		int baseAttack = 0;
		int bonus = 0;
		HitType ht = HitType.PHHIT;
		if (!useTemplateDmg) {
			if (effector instanceof Npc && !(effector instanceof Servant)) {
				ht = effect.getSkillType() == SkillType.MAGICAL ? HitType.MAHIT : HitType.PHHIT;
				baseAttack = effector.getGameStats().getMainHandPAttack().getBase();

				// should we calculate damage always? usually only if ht == PHHIT
				damage = StatFunctions.calculatePhysicalAttackDamage(effect.getEffector(), effect.getEffected(), true, true);
			} else {
				switch (effect.getSkillType()) {
					case MAGICAL:
						ht = HitType.MAHIT;
						baseAttack = effector.getGameStats().getMainHandMAttack().getBase();
						if (baseAttack == 0 && effector.getAttackType() == ItemAttackType.PHYSICAL) { // dirty fix for staffs and maces -.-
							baseAttack = effector.getGameStats().getMainHandPAttack().getBase();
							if (element == SkillElement.NONE) { // fix for magical skills which actually inflict physical damage
								damage = StatFunctions.calculatePhysicalAttackDamage(effect.getEffector(), effect.getEffected(), true, true);
							}
						}
						break;
					default:
						baseAttack = effector.getGameStats().getMainHandPAttack().getBase();
						damage = StatFunctions.calculatePhysicalAttackDamage(effect.getEffector(), effect.getEffected(), true, true);
						break;
				}
			}
		}

		// add skill damage
		if (func != null) {
			switch (func) {
				case ADD:
					damage += skillDamage;
					break;
				case PERCENT:
					damage += Math.round(baseAttack * skillDamage / 100f);
					break;
			}
		}

		// add bonus damage
		if (modifier != null) {
			bonus = modifier.analyze(effect);
			switch (modifier.getFunc()) {
				case ADD:
					break;
				case PERCENT:
					bonus = Math.round(baseAttack * bonus / 100f);
					break;
			}
		}

		if (!useTemplateDmg) {
			float damageMultiplier;
			switch (element) {
				case NONE:
					damageMultiplier = effector.getObserveController().getBasePhysicalDamageMultiplier(true);
					damage += bonus;
					break;
				default:
					damageMultiplier = effector.getObserveController().getBaseMagicalDamageMultiplier();
					damage = StatFunctions.calculateMagicalSkillDamage(effector, effected, damage, bonus, element, true, true);
					break;
			}
			damage = StatFunctions.adjustDamageByMovementModifier(effector, damage);
			damage = Math.round(damage * damageMultiplier);
		}
		damage = StatFunctions.adjustDamageByPvpOrPveModifiers(effector, effected, damage, effect.getPvpDamage(), useTemplateDmg, element);

		if (randomDamageType > 0)
			damage = randomizeDamage(randomDamageType, damage);

		AttackStatus status;
		switch (element) {
			case NONE:
				status = calculatePhysicalStatus(effector, effected, true, accMod, criticalProb, true, cannotMiss);
				break;
			default:
				status = calculateMagicalStatus(effector, effected, criticalProb, true);
				break;
		}

		switch (AttackStatus.getBaseStatus(status)) {
			case BLOCK:
				damage = calculateBlockedDamage(effected, damage);
				break;
			case PARRY:
				damage *= 0.6;
				break;
		}

		switch (element) {
			case NONE:
				switch (AttackStatus.getBaseStatus(status)) {
					case CRITICAL:
						damage = (int) calculateWeaponCritical(element, effected, damage, getWeaponGroup(effector, true), critAddDmg, StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE, true);
						break;
				}
				break;
			default:
				switch (status) {
					case CRITICAL:
						damage = (int) calculateWeaponCritical(element, effected, damage, getWeaponGroup(effector, true), critAddDmg, StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, true);
						break;
				}
				break;
		}

		if (effected instanceof Npc) {
			damage = effected.getAi().modifyDamage(effector, damage, effect);
		}
		if (effector instanceof Npc) {
			damage = effector.getAi().modifyOwnerDamage(damage, effected, effect);
		}

		if (shared && !effect.getSkill().getEffectedList().isEmpty())
			damage /= effect.getSkill().getEffectedList().size();

		if (damage < 0)
			damage = 0;

		calculateEffectResult(effect, effected, damage, status, ht, ignoreShield, position, send);
	}

	private static boolean isUseTemplateDmg(Effect effect, EffectTemplate template) {
		if (template instanceof NoReduceSpellATKInstantEffect)
			return true;
		if (template instanceof ProcAtkInstantEffect && effect.getSkillTemplate().isProvoked()) { // proc effects of skills like 8583
			if (!effect.getStack().startsWith("ITEM_SKILL_PROC") && !effect.getStack().startsWith("BA_N_")) // exclude godstones and bard skills
				return true;
		}
		return false;
	}

	private static int randomizeDamage(int randomDamageType, int damage) {
		switch (randomDamageType) {
			case 1:
				switch (Rnd.get(1, 3)) {
					case 1:
						damage *= 0.5f;
						break;
					case 2:
						damage *= 1.5f;
						break;
				}
				break;
			case 2:
				if (Rnd.chance() < 70)
					damage *= 0.6f;
				else
					damage *= 2;
				break;
			case 3:
				switch (Rnd.get(1, 3)) {
					case 1:
						damage *= 1.15f;
						break;
					case 2:
						damage *= 1.25f;
						break;
				}
				break;
			case 4:
				damage *= (Rnd.get(25, 100) * 0.02f);
				break;
			case 6:
				if (Rnd.chance() < 30)
					damage *= 2;
				break;
			default:
				throw new IllegalArgumentException("Unhandled random damage type rnddmg=\"" + randomDamageType + "\"");
		}
		return damage;
	}

	private static void calculateEffectResult(Effect effect, Creature effected, int damage, AttackStatus status, HitType hitType, boolean ignoreShield,
		int position, boolean send) {
		AttackResult attackResult = new AttackResult(damage, status, hitType);
		if (!ignoreShield) {
			effected.getObserveController().checkShieldStatus(Collections.singletonList(attackResult), effect, effect.getEffector());
			effect.setReflectedDamage(attackResult.getReflectedDamage());
			effect.setReflectedSkillId(attackResult.getReflectedSkillId());
			effect.setMpAbsorbed(attackResult.getMpAbsorbed());
			effect.setMpShieldSkillId(attackResult.getMpShieldSkillId());
			effect.setProtectedDamage(attackResult.getProtectedDamage());
			effect.setProtectedSkillId(attackResult.getProtectedSkillId());
			effect.setProtectorId(attackResult.getProtectorId());
			effect.setShieldDefense(attackResult.getShieldType());
		}
		effect.setReserveds(new EffectReserved(position, attackResult.getDamage(), ResourceType.HP, true, send), false);
		effect.setAttackStatus(attackResult.getAttackStatus());
		effect.setLaunchSubEffect(attackResult.isLaunchSubEffect());
	}

	public static List<AttackResult> calculateMagicalAttackResult(Creature attacker, Creature attacked, SkillElement elem) {

		AttackStatus attackerStatus = null;
		int damage = StatFunctions.calculateAttackDamage(attacker, attacked, true, elem);
		List<AttackResult> attackList = new ArrayList<>();
		AttackStatus mainHandStatus = calculateMainHandResult(attacker, attacked, attackerStatus, damage, attackList, elem);

		if (attacker instanceof Player && ((Player) attacker).getEquipment().getOffHandWeaponType() != null) {
			calculateOffHandResult(attacker, attacked, mainHandStatus, attackList, elem);
		}
		attacked.getObserveController().checkShieldStatus(attackList, null, attacker);
		return attackList;

	}

	public static List<AttackResult> calculateHomingAttackResult(Creature attacker, Creature attacked, SkillElement elem) {
		int damage = StatFunctions.calculateAttackDamage(attacker, attacked, true, elem);

		AttackStatus status = calculateHomingAttackStatus(attacker, attacked, elem);
		List<AttackResult> attackList = new ArrayList<>();
		switch (status) {
			case RESIST:
			case DODGE:
				damage = 0;
				break;
			case PARRY:
				damage *= 0.6;
				break;
			case BLOCK:
				damage /= 2;
				break;
		}
		attackList.add(new AttackResult(damage, status));
		attacked.getObserveController().checkShieldStatus(attackList, null, attacker);
		return attackList;
	}

	/**
	 * @param effect
	 * @param skillDamage
	 * @param element
	 * @param position
	 * @param useMagicBoost
	 * @param criticalProb
	 * @param critAddDmg
	 * @return
	 */
	public static int calculateMagicalOverTimeSkillResult(Effect effect, int skillDamage, SkillElement element, int position, boolean useMagicBoost,
		int criticalProb, int critAddDmg) {
		Creature effector = effect.getEffector();
		Creature effected = effect.getEffected();
		int damage;

		if (effector instanceof Trap) {
			damage = skillDamage;
		} else {
			// TODO is damage multiplier used on dot?
			float damageMultiplier = effector.getObserveController().getBaseMagicalDamageMultiplier();
			damage = StatFunctions.calculateMagicalSkillDamage(effector, effected, skillDamage, 0, element, useMagicBoost, false);
			damage = Math.round(damage * damageMultiplier);
			damage = StatFunctions.adjustDamageByPvpOrPveModifiers(effector, effected, damage, effect.getPvpDamage(), false, element);

			AttackStatus status = effect.getAttackStatus();
			// calculate attack status only if it has not been forced already
			if (status == AttackStatus.NORMALHIT && position == 1)
				status = calculateMagicalStatus(effector, effected, criticalProb, true);
			switch (status) {
				case CRITICAL:
					damage = (int) calculateWeaponCritical(element, effected, damage, getWeaponGroup(effector, true), critAddDmg,
						StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, true);
					break;
			}
		}

		if (damage <= 0)
			damage = 1;

		if (effected instanceof Npc)
			damage = effected.getAi().modifyDamage(effector, damage, effect);

		return damage;
	}

	/**
	 * Manage attack status rate
	 * 
	 * @source http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009 -a.html
	 * @return AttackStatus
	 */
	public static AttackStatus calculatePhysicalStatus(Creature attacker, Creature attacked, boolean isMainHand) {
		return calculatePhysicalStatus(attacker, attacked, isMainHand, 0, 100, false, false);
	}

	public static AttackStatus calculatePhysicalStatus(Creature attacker, Creature attacked, boolean isMainHand, int accMod, int criticalProb,
		boolean isSkill, boolean cannotMiss) {
		AttackStatus status = AttackStatus.NORMALHIT;
		if (!isMainHand)
			status = AttackStatus.OFFHAND_NORMALHIT;

		if (!cannotMiss) {
			if (attacked instanceof Player && ((Player) attacked).getEquipment().isShieldEquipped()
				&& StatFunctions.calculatePhysicalBlockRate(attacker, attacked))// TODO accMod
				status = AttackStatus.BLOCK;
			// Parry can only be done with weapon, also weapon can have humanoid mobs,
			// but for now there isnt implementation of monster category
			else if (attacked instanceof Player && ((Player) attacked).getEquipment().getMainHandWeaponType() != null
				&& StatFunctions.calculatePhysicalParryRate(attacker, attacked))// TODO accMod
				status = AttackStatus.PARRY;
			else if (!isSkill && StatFunctions.calculatePhysicalDodgeRate(attacker, attacked, accMod)) {
				status = AttackStatus.DODGE;
			}
		} else {
			// Check AlwaysDodge Check AlwaysParry Check AlwaysBlock
			StatFunctions.calculatePhysicalDodgeRate(attacker, attacked, accMod);
			StatFunctions.calculatePhysicalParryRate(attacker, attacked);
			StatFunctions.calculatePhysicalBlockRate(attacker, attacked);
		}

		if (StatFunctions.calculatePhysicalCriticalRate(attacker, attacked, isMainHand, criticalProb, isSkill)) {
			switch (status) {
				case BLOCK:
					if (isMainHand)
						status = AttackStatus.CRITICAL_BLOCK;
					else
						status = AttackStatus.OFFHAND_CRITICAL_BLOCK;
					break;
				case PARRY:
					if (isMainHand)
						status = AttackStatus.CRITICAL_PARRY;
					else
						status = AttackStatus.OFFHAND_CRITICAL_PARRY;
					break;
				case DODGE:
					if (isMainHand)
						status = AttackStatus.CRITICAL_DODGE;
					else
						status = AttackStatus.OFFHAND_CRITICAL_DODGE;
					break;
				default:
					if (isMainHand)
						status = AttackStatus.CRITICAL;
					else
						status = AttackStatus.OFFHAND_CRITICAL;
					break;
			}
		}

		return status;
	}

	/**
	 * Every + 100 delta of (MR - MA) = + 10% to resist<br>
	 * if the difference is 1000 = 100% resist
	 */
	public static AttackStatus calculateMagicalStatus(Creature attacker, Creature attacked, int criticalProb, boolean isSkill) {
		if (!isSkill) {
			if (Rnd.get(1, 1000) <= StatFunctions.calculateMagicalResistRate(attacker, attacked, 0, SkillElement.NONE))
				return AttackStatus.RESIST;
		}

		if (StatFunctions.calculateMagicalCriticalRate(attacker, attacked, criticalProb)) {
			return AttackStatus.CRITICAL;
		}

		return AttackStatus.NORMALHIT;
	}

	private static AttackStatus calculateHomingAttackStatus(Creature attacker, Creature attacked, SkillElement element) {
		if (Rnd.get(1, 1000) <= StatFunctions.calculateMagicalResistRate(attacker, attacked, 0, element))
			return AttackStatus.RESIST;

		else if (StatFunctions.calculatePhysicalDodgeRate(attacker, attacked, 0))
			return AttackStatus.DODGE;

		else if (StatFunctions.calculatePhysicalParryRate(attacker, attacked))
			return AttackStatus.PARRY;

		else if (StatFunctions.calculatePhysicalBlockRate(attacker, attacked))
			return AttackStatus.BLOCK;

		else
			return AttackStatus.NORMALHIT;

	}

	public static void cancelCastOn(Creature target) {
		target.getKnownList().forEachObject(visibleObject -> {
			if (visibleObject instanceof Creature && visibleObject.getTarget() == target) {
				Creature creature = (Creature) visibleObject;
				if (creature.getCastingSkill() != null && creature.getCastingSkill().getFirstTarget().equals(target))
					creature.getController().cancelCurrentSkill(null);
			}
		});
	}

	/**
	 * Send a packet to everyone who is targeting creature.
	 * 
	 * @param object
	 */
	public static void removeTargetFrom(Creature object) {
		removeTargetFrom(object, false);
	}

	public static void removeTargetFrom(Creature object, boolean validateSee) {
		object.getKnownList().forEachPlayer(player -> {
			if (player.getTarget() == object) {
				if (!validateSee || !player.canSee(object)) {
					player.setTarget(null);
					PacketSendUtility.sendPacket(player, new SM_TARGET_SELECTED(player));
				}
			}
		});
	}

	private static ItemGroup getWeaponGroup(Creature effector, boolean mainHand) {
		if (effector instanceof Player) {
			Item weapon = mainHand ? ((Player) effector).getEquipment().getMainHandWeapon() : ((Player) effector).getEquipment().getOffHandWeapon();
			if (weapon != null) {
				return weapon.getItemTemplate().getItemGroup();
			}
		} else if (effector instanceof Npc) {
			NpcTemplate temp = DataManager.NPC_DATA.getNpcTemplate(((Npc) effector).getNpcId());
			NpcEquippedGear npcGear = temp.getEquipment();
			if (npcGear != null && npcGear.getItem(mainHand ? ItemSlot.MAIN_HAND : ItemSlot.MAIN_OFF_HAND) != null) {
				return npcGear.getItem(mainHand ? ItemSlot.MAIN_HAND : ItemSlot.MAIN_OFF_HAND).getItemGroup();
			}
		}
		return null;
	}
}
