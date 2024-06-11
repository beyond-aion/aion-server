package com.aionemu.gameserver.controllers.attack;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.NpcEquippedGear;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemAttackType;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.model.templates.npc.NpcTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.aionemu.gameserver.skillengine.change.Func;
import com.aionemu.gameserver.skillengine.effect.*;
import com.aionemu.gameserver.skillengine.effect.modifier.ActionModifier;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.EffectReserved;
import com.aionemu.gameserver.skillengine.model.EffectReserved.ResourceType;
import com.aionemu.gameserver.skillengine.model.HitType;
import com.aionemu.gameserver.skillengine.model.SkillType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.stats.CalculationType;
import com.aionemu.gameserver.utils.stats.StatFunctions;

/**
 * @author ATracer
 */
public class AttackUtil {

	/**
	 * This method calculates the physical attack status and damage in the following order: <br>
	 * 1. calculate status<br>
	 * 2. calculate main & off hand damage<br>
	 * 3. apply stat modifiers<br>
	 * 4. amplify damage by hit count<br>
	 * @param attacker Creature attacking
	 * @param attacked Creature being attacked
	 * @param calculationTypes
	 * @return {@code List<AttackResult>} containing the results for each hand
	 */
	public static List<AttackResult> calculatePhysAttackResult(Creature attacker, Creature attacked, CalculationType... calculationTypes) {
		AttackStatus attackStatus = calculatePhysicalStatus(attacker, attacked, true, 0, 100, false, false);
		List<AttackResult> attackResultList = StatFunctions.calculateAttackDamage(attacker, SkillElement.NONE, attackStatus, calculationTypes);
		adjustDamageByStatModifiers(attacker, attacked, attackStatus, attackResultList, SkillElement.NONE);
		amplifyDamageByAdditionalHitCount(attacker, attackStatus, attackResultList);
		modifyDamageByNpcAi(attacker, attacked, attackResultList);
		attacked.getObserveController().checkShieldStatus(attackResultList, null, attacker);
		return attackResultList;
	}

	public static void adjustDamageByStatModifiers(Creature attacker, Creature attacked, AttackStatus status, List<AttackResult> attackResultList, SkillElement element) {
		float mainMultiplier = 1;
		float offMultiplier = 1;
		int reduceMax = Integer.MAX_VALUE;
		float reduceRatio = 0;
		switch (AttackStatus.getBaseStatus(status)) {
			case DODGE:
				return;
			case BLOCK:
				if (attacked instanceof Player p) {
					Item shield = p.getEquipment().getEquippedShield();
					if (shield != null) {
						reduceMax = shield.getItemTemplate().getWeaponStats().getReduceMax();
						reduceRatio = attacked.getGameStats().getReverseStat(StatEnum.DAMAGE_REDUCE, 100).getCurrent() / 100f;
					}
				} else {
					reduceRatio = 10; // NPCs reduce damage by min. 10%. TODO: Implement blocking for npcs without shield + check ratio for different npcs
				}
				break;
			case PARRY:
				mainMultiplier *= 0.6f;
				offMultiplier *= 0.6f;
				break;
		}

		if (status.isCritical()) {
			mainMultiplier = 1.5f;
			if (element == SkillElement.NONE) {
				ItemGroup mainHandGroup = getWeaponGroup(attacker, true);
				if (mainHandGroup != null) {
					mainMultiplier = getWeaponMultiplier(mainHandGroup);
					ItemGroup offHandGroup = getWeaponGroup(attacker, false);
					if (offHandGroup != null) {
						offMultiplier = getWeaponMultiplier(offHandGroup);
					}
				}
			}
			if (attacked instanceof Player) {
				int fortitude;
				if (element == SkillElement.NONE) { // if stat != null ? why
					fortitude = attacked.getGameStats().getStat(StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE, 0).getCurrent();
				} else {
					fortitude = attacked.getGameStats().getStat(StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, 0).getCurrent();
				}
				mainMultiplier = (mainMultiplier - fortitude / 1000f);
				offMultiplier = (offMultiplier + fortitude / 1000f);
			}
		}

		int maxListIndex = Math.min(attackResultList.size(), 2);
		if (maxListIndex < attackResultList.size()) // should never happen but log just in case
			LoggerFactory.getLogger(AttackUtil.class).warn("attackResultList has more elements than expected (" + attackResultList.size() + ")");
		for (int i = 0; i < maxListIndex; i++) {
			StatEnum defenseStat = element == SkillElement.NONE ? StatEnum.PHYSICAL_DEFENSE : StatEnum.MAGICAL_DEFEND;
			float def = attacked.getGameStats().getPDef().getBonus() + StatFunctions.getMovementModifier(attacked, defenseStat,
				defenseStat == StatEnum.PHYSICAL_DEFENSE ? attacked.getGameStats().getPDef().getBase() : attacked.getGameStats().getMDef().getBase());
			float damage = (StatFunctions.adjustDamageByMovementModifier(attacker,attackResultList.get(i).getDamage()) - (def/10)) * (i == 0 ? mainMultiplier : offMultiplier);
			if (reduceRatio > 0) {
				float dmgToReduce = damage - (damage * reduceRatio);
				if (dmgToReduce > reduceMax) {
					dmgToReduce = reduceMax;
				}
				damage -= dmgToReduce;
			}
			damage = StatFunctions.adjustDamageByPvpOrPveModifiers(attacker, attacked, damage, 0, false, element);
			if (damage < 1) {
				damage = 1;
			}
			attackResultList.get(i).setDamage(damage);
		}
	}

	private static int[] calculateAdditionalHitCount(Creature attacker, AttackStatus status, List<AttackResult> attackList) {
		int[] hitCount = new int[2];
		if (attacker instanceof Player p && (status != AttackStatus.DODGE && status != AttackStatus.RESIST)) {
			Item mainHandWeapon = p.getEquipment().getMainHandWeapon();
			if (mainHandWeapon != null) {
				hitCount[0] = Rnd.get(0, mainHandWeapon.getItemTemplate().getWeaponStats().getHitCount()) - 1;
				if (attackList.size() > 1) {
					Item offHandWeapon = p.getEquipment().getOffHandWeapon();
					if (offHandWeapon != null && offHandWeapon.getItemTemplate().getItemSubType() != ItemSubType.SHIELD) {
						hitCount[1] = Rnd.get(0, offHandWeapon.getItemTemplate().getWeaponStats().getHitCount() - 1);
					}
				}
			}

		}
		return hitCount;
	}

	private static void amplifyDamageByAdditionalHitCount(Creature attacker, AttackStatus status, List<AttackResult> attackList) {
		int[] hitCount = calculateAdditionalHitCount(attacker, status, attackList);
		for (int i = 0; i < hitCount[0] + hitCount[1]; i++) {
			if (i < hitCount[0]) { // amplify main hand damage
				if (attackList.get(0).getDamage() >= 10)
					attackList.add(new AttackResult((int) (attackList.get(0).getDamage() * 0.1), AttackStatus.NORMALHIT, attackList.get(0).getHitType()));
			} else { // amplify off hand damage
				if (attackList.get(1).getDamage() >= 10)
					attackList.add(new AttackResult((int)(attackList.get(1).getDamage() * 0.1), AttackStatus.OFFHAND_NORMALHIT, attackList.get(1).getHitType()));
			}
		}
	}

	private static void modifyDamageByNpcAi(Creature attacker, Creature attacked, List<AttackResult> attackStatus) {
		if (!(attacker instanceof Npc || attacked instanceof Npc))
			return;
		for (AttackResult status : attackStatus) {
			float modifiedDamage = status.getDamage();
			if (attacker instanceof Npc)
				modifiedDamage = attacker.getAi().modifyOwnerDamage(modifiedDamage, attacked, null);
			if (attacked instanceof Npc)
				modifiedDamage = attacked.getAi().modifyDamage(attacker, modifiedDamage, null);
			status.setDamage(modifiedDamage);
		}
	}

	private static float calculateBlockedDamage(Creature attacked, float damage) {
		int reduceStat = attacked.getGameStats().getReverseStat(StatEnum.DAMAGE_REDUCE, 100).getCurrent();
		float reduceVal = damage - (damage * reduceStat / 100);
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

	private static float calculateWeaponCritical(SkillElement element, Creature attacked, float damage, ItemGroup group, int critAddDmg, StatEnum stat, boolean isMain) {
		float coeficient = 1.5f;
		if (element == SkillElement.NONE && group != null) {
			coeficient = getWeaponMultiplier(group);
		}

		if (stat != null && attacked instanceof Player) { // Strike Fortitude lowers the crit multiplier
			switch (stat) {
				case PHYSICAL_CRITICAL_DAMAGE_REDUCE, MAGICAL_CRITICAL_DAMAGE_REDUCE -> {
					int fortitude = attacked.getGameStats().getStat(stat, 0).getCurrent();
					coeficient = isMain ? (coeficient - fortitude / 1000f) : (coeficient + fortitude / 1000f);
				}
			}
		}

		// add critical add dmg
		coeficient += critAddDmg / 100f;
		return damage * coeficient;
	}

	private static float getWeaponMultiplier(ItemGroup group) {
		return switch (group) {
			case DAGGER -> 2.3f;
			case SWORD -> 2.2f;
			case MACE -> 2f;
			case GREATSWORD, POLEARM -> 1.8f;
			case STAFF, BOW -> 1.7f;
			default -> 1.5f;
		};
	}

	public static void calculateSkillResult(Effect effect, int skillDamage, EffectTemplate template, boolean ignoreShield) {
		Creature effector = effect.getEffector();
		Creature effected = effect.getEffected();
		// define values
		ActionModifier modifier = template.getActionModifiers(effect);
		SkillElement element = template.getElement();
		Func func = template instanceof DamageEffect damageEffect ? damageEffect.getMode() : Func.ADD;
		int randomDamageType = template instanceof SkillAttackInstantEffect skillAttackInstantEffect ? skillAttackInstantEffect.getRnddmg() : 0;
		int critAddDmg = template.getCritAddDmg2() + template.getCritAddDmg1() * effect.getSkillLevel();
		boolean useTemplateDmg = isUseTemplateDmg(effect, template);
		boolean send = !(template instanceof DelayedSpellAttackInstantEffect) && !(template instanceof ProcAtkInstantEffect);
		boolean shouldIncreaseByOneTimeBoost = !(template instanceof ProcAtkInstantEffect);

		AttackStatus status;
		switch (element) {
			case NONE:
				status = calculatePhysicalStatus(effector, effected, template, effect.getSkillLevel());
				break;
			default:
				status = calculateMagicalStatus(effector, effected, template.getCritProbMod2(), true, effect.getSkillTemplate().isMcritApplied());
				break;
		}

		int baseAttack = 0;
		float bonus = 0;
		HitType ht = HitType.PHHIT;
		List<AttackResult> weaponAttack = new ArrayList<>();
		float damage = 0;
		CalculationType[] calculationTypes = new CalculationType[] { CalculationType.SKILL };
		if (effector instanceof Player p && p.getEquipment().hasDualWeaponEquipped(ItemSlot.LEFT_HAND))
			calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.DUAL_WIELD);
		if (!useTemplateDmg) {
			if (effector instanceof SummonedObject && !(effector instanceof Servant)) {
				ht = effect.getSkillType() == SkillType.MAGICAL ? HitType.MAHIT : HitType.PHHIT;
				baseAttack = effector.getGameStats().getMainHandPAttack(calculationTypes).getBase();
				weaponAttack = StatFunctions.calculateAttackDamage(effect.getEffector(), SkillElement.NONE, status, calculationTypes);
			} else {
				switch (effect.getSkillType()) {
					case MAGICAL:
						ht = HitType.MAHIT;
						baseAttack = effector.getGameStats().getMainHandMAttack(calculationTypes).getBase();
						if (baseAttack == 0 && effector.getAttackType() == ItemAttackType.PHYSICAL) { // dirty fix for staffs and maces -.-
							calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.APPLY_POWER_SHARD_DAMAGE);
							if (element == SkillElement.NONE) { // fix for magical skills which actually inflict physical damage
								calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.REMOVE_POWER_SHARD);
								weaponAttack = StatFunctions.calculateAttackDamage(effect.getEffector(), SkillElement.NONE, status, calculationTypes);
								calculationTypes = ArrayUtils.removeElement(calculationTypes, CalculationType.REMOVE_POWER_SHARD); // remove to prevent power shards being removed again in baseAttack calculation
							} else {
								calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.REMOVE_POWER_SHARD);
							}
							baseAttack = effector.getGameStats().getMainHandPAttack(calculationTypes).getBase();
						}
						break;
					default:
						if (element == SkillElement.NONE) {
							calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.APPLY_POWER_SHARD_DAMAGE);
							baseAttack = effector.getGameStats().getMainHandPAttack(calculationTypes).getBase();
							calculationTypes = ArrayUtils.add(calculationTypes, CalculationType.REMOVE_POWER_SHARD);
							weaponAttack = StatFunctions.calculateAttackDamage(effect.getEffector(), SkillElement.NONE, status, calculationTypes);
						} else {
							baseAttack = effector.getGameStats().getMainHandMAttack(calculationTypes).getBase();
						}
						break;
				}
			}
		}
		for (AttackResult res : weaponAttack) {
			damage += res.getExactDamage();
		}
		// add skill damage
		if (func != null) {
			switch (func) {
				case ADD -> damage += skillDamage;
				case PERCENT -> damage += baseAttack * skillDamage / 100f;
			}
		}

		// add bonus damage
		if (modifier != null) {
			bonus = modifier.analyze(effect);
			switch (modifier.getFunc()) {
				case ADD:
					break;
				case PERCENT:
					bonus = baseAttack * bonus / 100f;
					break;
			}
		}

		if (!useTemplateDmg) {
			float damageMultiplier;
			switch (element) {
				case NONE -> {
					damageMultiplier = effector.getObserveController().getBasePhysicalDamageMultiplier(true);
					damage += bonus;
				}
				default -> {
					damageMultiplier = shouldIncreaseByOneTimeBoost ? effector.getObserveController().getBaseMagicalDamageMultiplier() : 1f;
					damage = StatFunctions.calculateMagicalSkillDamage(effector, effected, damage, (int) bonus, element, true, true);
				}
			}
			damage = StatFunctions.adjustDamageByMovementModifier(effector, damage);
			damage *= damageMultiplier;
		}

		if (randomDamageType > 0)
			damage = randomizeDamage(randomDamageType, damage);

		damage = switch (status) {
			case CRITICAL_BLOCK, CRITICAL_PARRY, CRITICAL -> calculateWeaponCritical(element, effected, damage, getWeaponGroup(effector, true), critAddDmg, element == SkillElement.NONE ?
						StatEnum.PHYSICAL_CRITICAL_DAMAGE_REDUCE : StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, true);
			default -> damage;
		};

		if (element == SkillElement.NONE) {
			float def = effected.getGameStats().getPDef().getBonus() + StatFunctions.getMovementModifier(effected, StatEnum.PHYSICAL_DEFENSE,
					effected.getGameStats().getPDef().getBase());
			damage -= def/10;
		}

		switch (AttackStatus.getBaseStatus(status)) {
			case BLOCK -> damage = calculateBlockedDamage(effected, damage);
			case PARRY -> damage *= 0.6f;
		}

		if (effector instanceof Npc) {
			damage = effector.getAi().modifyOwnerDamage(damage, effected, effect);
		}

		if (effect.getSkill() != null && effect.getSkill().getEffectedList().size() > 1 && template instanceof DamageEffect damageEffect && damageEffect.isShared()) {
			damage /= effect.getSkill().getEffectedList().size();
		}
		damage = StatFunctions.adjustDamageByPvpOrPveModifiers(effector, effected, damage, effect.getPvpDamage(), useTemplateDmg, element);

		if (damage < 0)
			damage = 0;

		if (effected instanceof Npc) {
			damage = effected.getAi().modifyDamage(effector, damage, effect);
		}
		calculateEffectResult(effect, effected, (int) damage, status, ht, ignoreShield, template.getPosition(), send);
	}

	private static boolean isUseTemplateDmg(Effect effect, EffectTemplate template) {
		if (template instanceof NoReduceSpellATKInstantEffect)
			return true;
		if (template instanceof ProcAtkInstantEffect && effect.getSkillTemplate().isProvoked() || effect.getStack().startsWith("IDEVENT")) { // proc effects of skills like 8583
			// TODO: find pattern or extract <apply_magical_skill_boost_bonus> and <apply_magical_critical> from server files. What about missing ones?
			switch (effect.getStack().toLowerCase()) {
				case "nwi_delayspell_dd_proca_tal":
				case "ngu_vritra_delayspell_dd_proca_tal":
				case "sgfi_procts_air":
				case "ab1_artifact_hellfire":
				case "ldf4b_c3_artifact_tiamat_delayatk":
				case "ldf4b_t4_artifact_crystal_dd":
				case "ldf4b_t3_artifact_fury_dd":
				case "ldf4b_t2_artifact_gravity_openaerial":
				case "ldf4b_t2_artifact_gravity_dd":
				case "ldf4b_t1_artifact_crack_stumble_mpatk":
				case "ldf4b_t1_artifact_crack_dd":
				case "idtiamat_tahabata_adddmgtobleed":
				case "kn_turnaggressiveeffect":
				case "tiamatdown_tiamatagent_bomb":
				case "idtiamat_thor_procatk":
				case "idyun_vasharti_refdmg_red":
				case "idyun_vasharti_refdmg_blue":
				case "ldf4b_d3_buff_poison_proc":
				case "ldf4b_tatar_procatk":
				case "idforest_wave_trico_proclight":
				case "idevent01_areadot":
					return true;
			}
		}
		return false;
	}

	private static float randomizeDamage(int randomDamageType, float damage) {
		switch (randomDamageType) {
			case 1:
					switch (Rnd.get(1, 3)) {
							case 1 -> damage *= 0.5f;
							case 2 -> damage *= 1.5f;
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
							case 1 -> damage *= 1.15f;
							case 2 -> damage *= 1.25f;
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

	/**
	 * This method calculates the magical attack status and damage in the following order:<br>
	 * 1. calculate status<br>
	 * 2. calculate main & off hand damage<br>
	 * 3. apply stat modifiers<br>
	 * 4. amplify damage by hit count<br>
	 * @param attacker Creature attacking
	 * @param attacked Creature being attacked
	 * @param calculationTypes
	 * @return {@code List<AttackResult>} containing the results for each hand
	 */
	public static List<AttackResult> calculateMagAttackResult(Creature attacker, Creature attacked, SkillElement element, CalculationType... calculationTypes) {
		AttackStatus attackStatus = calculateMagicalStatus(attacker, attacked, 100, false, true);
		List<AttackResult> attackResultList = StatFunctions.calculateAttackDamage(attacker, element, attackStatus, calculationTypes);
		adjustDamageByStatModifiers(attacker, attacked, attackStatus, attackResultList, element);
		amplifyDamageByAdditionalHitCount(attacker, attackStatus, attackResultList);
		modifyDamageByNpcAi(attacker, attacked, attackResultList);
		attacked.getObserveController().checkShieldStatus(attackResultList, null, attacker);
		return attackResultList;
	}

	public static int calculateMagicalOverTimeSkillResult(Effect effect, float skillDamage, SkillElement element, int position, boolean useMagicBoost,
														  int criticalProb, int critAddDmg) {
		Creature effector = effect.getEffector();
		Creature effected = effect.getEffected();
		float damage;

		if (effector instanceof Trap) {
			damage = skillDamage;
		} else {
			// TODO is damage multiplier used on dot?
			float damageMultiplier = effector.getObserveController().getBaseMagicalDamageMultiplier();
			damage = StatFunctions.calculateMagicalSkillDamage(effector, effected, skillDamage, 0, element, useMagicBoost, false);
			damage = damage * damageMultiplier;

			AttackStatus status = effect.getAttackStatus();
			// calculate attack status only if it has not been forced already
			if (status == AttackStatus.NORMALHIT && position == 1)
				status = calculateMagicalStatus(effector, effected, criticalProb, true, effect.getSkillTemplate().isMcritApplied());
			switch (status) {
				case CRITICAL:
					damage = calculateWeaponCritical(element, effected, damage, getWeaponGroup(effector, true), critAddDmg,
						StatEnum.MAGICAL_CRITICAL_DAMAGE_REDUCE, true);
					break;
			}
			damage = StatFunctions.adjustDamageByPvpOrPveModifiers(effector, effected, damage, effect.getPvpDamage(), false, element);
		}

		if (damage < 1)
			damage = 1;

		if (effected instanceof Npc)
			damage = effected.getAi().modifyDamage(effector, damage, effect);

		return (int) damage;
	}

	private static AttackStatus calculatePhysicalStatus(Creature attacker, Creature attacked, EffectTemplate template, int skillLevel) {
		int accMod = template.getAccMod2() + template.getAccMod1() * skillLevel;
		boolean cannotMiss = template instanceof SkillAttackInstantEffect skillAttackInstantEffect && skillAttackInstantEffect.isCannotmiss();
		return calculatePhysicalStatus(attacker, attacked, true, accMod, template.getCritProbMod2(), true, cannotMiss);
	}

	private static AttackStatus calculatePhysicalStatus(Creature attacker, Creature attacked, boolean isMainHand, int accMod, int criticalProb,
		boolean isSkill, boolean cannotMiss) {
		AttackStatus status = AttackStatus.NORMALHIT;

		if (!cannotMiss) {
			if (!isSkill && StatFunctions.checkIsDodgedHit(attacker, attacked, accMod))
				status = AttackStatus.DODGE;
			else if (attacked instanceof Player player && player.getEquipment().isShieldEquipped()
				&& StatFunctions.checkIsBlockedHit(attacker, attacked, accMod))
				status = AttackStatus.BLOCK;
			else if (attacked instanceof Player && StatFunctions.checkIsParriedHit(attacker, attacked, accMod))
				status = AttackStatus.PARRY;
		} else {
			StatFunctions.checkIsDodgedHit(attacker, attacked, accMod);
			StatFunctions.checkIsBlockedHit(attacker, attacked, accMod);
			StatFunctions.checkIsParriedHit(attacker, attacked, accMod);
		}
		if (StatFunctions.checkIsPhysicalCriticalHit(attacker, attacked, isMainHand, criticalProb, isSkill)) {
			status = switch (status) {
				case BLOCK -> AttackStatus.CRITICAL_BLOCK;
				case PARRY -> AttackStatus.CRITICAL_PARRY;
				case DODGE -> AttackStatus.CRITICAL_DODGE;
				default -> AttackStatus.CRITICAL;
			};
		}
		return isMainHand ? status : AttackStatus.getOffHandStats(status);
	}

	/**
	 * Every + 100 delta of (MR - MA) = + 10% to resist<br>
	 * if the difference is 1000 = 100% resist
	 */
	public static AttackStatus calculateMagicalStatus(Creature attacker, Creature attacked, int criticalProb, boolean isSkill, boolean applyMcrit) {
		if (!isSkill) {
			if (Rnd.get(1, 1000) <= StatFunctions.calculateMagicalResistRate(attacker, attacked, 0, SkillElement.NONE))
				return AttackStatus.RESIST;
		}

		if (StatFunctions.calculateMagicalCriticalRate(attacker, attacked, criticalProb, applyMcrit)) {
			return AttackStatus.CRITICAL;
		}

		return AttackStatus.NORMALHIT;
	}

	public static void cancelCastOn(Creature target) {
		target.getKnownList().forEachObject(visibleObject -> {
			if (visibleObject instanceof Creature creature && visibleObject.getTarget() == target) {
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
					PacketSendUtility.sendPacket(player, new SM_TARGET_SELECTED(null));
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
