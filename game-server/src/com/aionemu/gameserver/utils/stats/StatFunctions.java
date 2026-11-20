package com.aionemu.gameserver.utils.stats;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.movement.MovementModifierDirection;
import com.aionemu.gameserver.controllers.movement.MovementModifierState;
import com.aionemu.gameserver.controllers.observer.AttackerCriticalStatus;
import com.aionemu.gameserver.controllers.observer.OneTimeBoostSkillAttack;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatCapUtil;
import com.aionemu.gameserver.model.stats.container.*;
import com.aionemu.gameserver.model.templates.item.WeaponStats;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.model.templates.abyss.AbyssRankTemplate;
import com.aionemu.gameserver.model.templates.npc.FirstAttackSplitRate;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.NoReduceSpellATKInstantEffect;
import com.aionemu.gameserver.skillengine.model.HitType;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * Calculations are based on the following research:<br>
 * original: <a href="http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009-a.html">Link</a>
 * <br>
 * backup:
 * <a href="http://web.archive.org/web/20120111184941/http://www.aionsource.com/topic/40542-character-stats-xp-dp-origin-gerbatorteam-july-2009/">Link
 * </a>
 * 
 * @author ATracer, alexa026, Neon
 */
public class StatFunctions {

	/**
	 * Base DP of a defeated player, the killer side value that is not part of the rank data.
	 */
	private static final int PVP_DP_BASE = 500;

	/**
	 * @return XP reward from target for a player of the given level, before it is split between the attackers
	 */
	public static long calculateExperienceReward(int playerLevel, Npc target) {
		return calculateExperienceReward(playerLevel, target, 1, 1, true);
	}

	/**
	 * @return XP reward from target for a player of the given level, reduced by the player's share in the kill
	 */
	public static long calculateExperienceReward(int playerLevel, Npc target, int damage, int totalDamage, boolean firstAttacker) {
		long baseXP = calculateBaseExperienceReward(target, damage, totalDamage, firstAttacker);
		return (long) (baseXP * DataManager.PC_EXP_MOD_TABLE.getMultiplier(playerLevel, target.getLevel()));
	}

	/**
	 * @return XP reward from target before it is reduced by the level difference to the receiving player
	 */
	public static long calculateBaseExperienceReward(Npc target, int damage, int totalDamage, boolean firstAttacker) {
		WorldMapInstance instance = target.getPosition().getWorldMapInstance();
		long baseXP = calculateExpShare(target.getObjectTemplate().getExp(), target.getObjectTemplate().getFirstAttackSplitRate(), damage, totalDamage,
			firstAttacker);
		return Math.round(baseXP * instance.getInstanceHandler().getExpMultiplier());
	}

	/**
	 * Part of a base XP reward that falls on one attacker or team, computed like the retail NPC server: who dealt all damage gets the whole base,
	 * otherwise the first attacker keeps its fixed share and the rest is split by damage, the base part in float and the damage share in double.
	 */
	public static long calculateExpShare(long base, FirstAttackSplitRate firstAttackSplitRate, int damage, int totalDamage, boolean firstAttacker) {
		if (damage == totalDamage)
			return base;
		float splitRate = firstAttackSplitRate.getRate();
		long share = (long) ((1 - splitRate) * base * ((double) damage / Math.max(1, totalDamage)));
		return firstAttacker ? share + (long) (base * splitRate) : share;
	}

	/**
	 * Part of a base DP, AP or GP reward that falls on one attacker or team, in float like the retail server: the damage part is multiplied by the
	 * damage before it is divided by the total damage, the first attacker's part is truncated. The caller multiplies it by its modifiers and
	 * truncates once; a team truncates it before sharing it, see {@link #calculateTeamPool}.
	 */
	public static float calculateRewardShare(long base, FirstAttackSplitRate firstAttackSplitRate, int damage, int totalDamage, boolean firstAttacker) {
		float splitRate = firstAttackSplitRate.getRate();
		float part = (1 - splitRate) * base * damage / Math.max(1, totalDamage);
		if (firstAttacker)
			part += (int) (base * splitRate);
		return part;
	}

	/**
	 * @return The reward share of a team, truncated as retail hands it to the team; AP and DP never round down to nothing if there is a base.
	 */
	public static int calculateTeamPool(long base, FirstAttackSplitRate firstAttackSplitRate, int damage, int totalDamage, boolean firstAttacker,
		boolean atLeastOne) {
		if (base <= 0)
			return 0;
		int pool = (int) calculateRewardShare(base, firstAttackSplitRate, damage, totalDamage, firstAttacker);
		return atLeastOne ? Math.max(1, pool) : pool;
	}

	/**
	 * @return XP of a team member, in the order of the retail party distribution, before XP boosts and rates.
	 */
	public static long calculateTeamMemberXp(long teamXp, float teamShare, float levelMod) {
		return (long) (100f * (teamShare * levelMod) / 100f * teamXp);
	}

	/**
	 * @param apMod
	 *          The level difference modifier times the map AP multiplier.
	 * @return AP of a team member, at least one, before the rank difference modifier, the limits and the rates.
	 */
	public static int calculateTeamMemberAp(int teamAp, float teamShare, float apMod) {
		return Math.max(1, (int) (teamShare * (teamAp * apMod)));
	}

	/**
	 * @return DP of a team member, at least one.
	 */
	public static int calculateTeamMemberDp(int teamDp, float teamShare, float levelMod, int memberLevel) {
		return Math.max(1, (int) (getDPLevelFactor(memberLevel) * levelMod * teamDp * teamShare));
	}

	/**
	 * @return GP of a team member, before rates.
	 */
	public static int calculateTeamMemberGp(int teamGp, float teamShare, float levelMod) {
		return (int) (teamShare * teamGp * levelMod);
	}

	/**
	 * @return DP reward from target, split between the attackers the same way as experience
	 */
	public static int calculateDPReward(Player player, Npc target, int damage, int totalDamage, boolean firstAttacker) {
		float part = calculateRewardShare(target.getObjectTemplate().getDp(), target.getObjectTemplate().getFirstAttackSplitRate(), damage, totalDamage,
			firstAttacker);
		float mod = DataManager.PC_EXP_MOD_TABLE.getMultiplier(player.getLevel(), target.getLevel());
		return (int) (part * (mod * getDPLevelFactor(player.getLevel())));
	}

	/**
	 * @return DP gain multiplier by the player level; players below level 10 get no DP at all
	 */
	public static float getDPLevelFactor(int playerLevel) {
		return playerLevel >= 10 ? 1 + 0.02f * (playerLevel - 10) : 0;
	}

	/**
	 * @return AP reward from target, split between the attackers the same way as experience, before the rank difference modifier, the limits and the
	 *         rates of {@link com.aionemu.gameserver.services.abyss.AbyssPointsService#addKillAp}
	 */
	public static int calculatePveApReward(Player player, Npc target, int damage, int totalDamage, boolean firstAttacker, float apMultiplier) {
		float part = calculateRewardShare(target.getObjectTemplate().getAbyssPoint(), target.getObjectTemplate().getFirstAttackSplitRate(), damage,
			totalDamage, firstAttacker);
		float mod = DataManager.PC_EXP_MOD_TABLE.getMultiplier(player.getLevel(), target.getLevel());
		return (int) (part * (mod * apMultiplier));
	}

	/**
	 * Retail truncates the share before the level modifier here, unlike DP and AP.
	 *
	 * @return Glory points reward from target, split between the attackers the same way as experience
	 */
	public static int calculatePveGpReward(Player player, Npc target, int damage, int totalDamage, boolean firstAttacker) {
		int part = (int) calculateRewardShare(target.getObjectTemplate().getGloryPoint(), target.getObjectTemplate().getFirstAttackSplitRate(), damage,
			totalDamage, firstAttacker);
		float mod = DataManager.PC_EXP_MOD_TABLE.getMultiplier(player.getLevel(), target.getLevel());
		return Rates.GP.calcResult(player, (int) (part * mod));
	}

	/**
	 * @param apMultiplier
	 *          The multiplier of the killers' AP pool.
	 * @return AP of a defeated player for the whole kill: the drop point of its rank with the map modifier.
	 */
	public static long calculatePvpApBase(Player victim, float apMultiplier) {
		AbyssRankTemplate rank = getAbyssRank(victim);
		if (rank == null)
			return 0;
		return (long) (rank.getDropPoint() * (DataManager.ABYSS_POINT_WORLD_MOD_TABLE.getDropPointMod(victim.getWorldId(), victim.getRace()) * apMultiplier));
	}

	/**
	 * @param apPart
	 *          The killer's part of the victim's AP, see {@link #calculateRewardShare}.
	 * @return AP gained by one killer who is not in a team, before the rank difference modifier, the limits and the rates of
	 *         {@link com.aionemu.gameserver.services.abyss.AbyssPointsService#addKillAp}.
	 */
	public static int calculatePvpApReward(Player killer, Player victim, float apPart) {
		return (int) (apPart * DataManager.PVP_MOD_TABLE.getMultiplier(killer.getLevel(), victim.getLevel()));
	}

	/**
	 * @return DP gained by one killer who is not in a team.
	 */
	public static int calculatePvpDpReward(Player killer, Player victim, float dpPart) {
		float mod = DataManager.PVP_MOD_TABLE.getMultiplier(killer.getLevel(), victim.getLevel());
		return (int) (dpPart * (mod * getDPLevelFactor(killer.getLevel())));
	}

	/**
	 * @return The base DP of a defeated player.
	 */
	public static int getPvpDpBase() {
		return PVP_DP_BASE;
	}

	/**
	 * The loss follows what the killers earned relative to the full AP of the victim, as retail does: the AP of all killers after the rank difference
	 * modifier, but before the limits and their AP rates, divided by the AP one killer would get for the whole kill. Killers stopped by a limit
	 * still cost the victim its AP.
	 *
	 * @param killersAp
	 *          The sum of {@link com.aionemu.gameserver.services.abyss.AbyssPointsService#addKillAp} results of all killers.
	 * @param apMultiplier
	 *          The multiplier of the killers' AP pool.
	 * @return AP lost by the defeated player.
	 */
	public static int calculatePvPApLoss(Player victim, int killersAp, float apMultiplier) {
		AbyssRankTemplate rank = getAbyssRank(victim);
		long fullAp = calculatePvpApBase(victim, apMultiplier);
		if (rank == null || fullAp <= 0)
			return 0;
		long loss = killersAp * rank.getDeathPenaltyPoint() / fullAp;
		float worldMod = DataManager.ABYSS_POINT_WORLD_MOD_TABLE.getDeathPenaltyMod(victim.getWorldId(), victim.getRace());
		return Rates.AP_PVP_LOST.calcResult(victim, (int) (loss * worldMod));
	}

	private static AbyssRankTemplate getAbyssRank(Player player) {
		return DataManager.ABYSS_RANK_TABLE.get(player.getRace(), player.getAbyssRank().getRank().getId());
	}

	/**
	 * Applies BOOST_HATE modifiers from equipment and buffs
	 */
	public static int calculateHate(Creature creature, int value) {
		int boostHate = creature.getGameStats().getStat(StatEnum.BOOST_HATE, 100).getCurrent();
		return (int) ((long) value * (1000 + boostHate) / 1000);
	}

	public static List<AttackResult> calculateAttackDamage(Creature attacker, SkillElement element, AttackStatus status,
		Set<CalculationType> calculationTypes) {
		List<AttackResult> attackResultList = new ArrayList<>();
		if (AttackStatus.getBaseStatus(status) == AttackStatus.DODGE || AttackStatus.getBaseStatus(status) == AttackStatus.RESIST) {
			attackResultList.add(new AttackResult(0, AttackStatus.getBaseStatus(status)));
			return attackResultList;
		}
		Stat2 mainHandAttack;
		Stat2 offHandAttack = null;
		HitType hitType = HitType.PHHIT;
		if (element == SkillElement.NONE) {
			mainHandAttack = attacker.getGameStats().getMainHandPAttack(calculationTypes);
			if (attacker instanceof Player p)
				offHandAttack = p.getGameStats().getOffHandPAttack(calculationTypes);
		} else {
			hitType = HitType.MAHIT;
			mainHandAttack = attacker.getGameStats().getMainHandMAttack(calculationTypes);
			if (attacker instanceof Player p)
				offHandAttack = p.getGameStats().getOffHandMAttack(calculationTypes);
		}

		if (attacker instanceof Player p) {
			Equipment equipment = p.getEquipment();
			Item mainHandWeapon = equipment.getMainHandWeapon();
			if (mainHandWeapon != null) {
				Item offHandWeapon = equipment.getOffHandWeapon();
				WeaponStats mainWeaponStats = mainHandWeapon.getItemTemplate().getWeaponStats();
				WeaponStats offWeaponStats = (offHandWeapon == null || offHandWeapon.getItemTemplate().getItemSubType() == ItemSubType.SHIELD)
						? null : offHandWeapon.getItemTemplate().getWeaponStats();
				if (mainWeaponStats != null) {
					float mainHandDamage = mainHandAttack.getExactCurrent();
					float offHandDamage = offHandAttack.getExactCurrent();
					if (calculationTypes.contains(CalculationType.SKILL)) { // 80% of damage is added on retail
						if (offWeaponStats != null) {
							float totalBaseDamage = (offHandAttack.getExactBaseWithoutBaseRate() * p.getGameStats().getSkillEfficiency() + mainHandAttack.getExactBaseWithoutBaseRate()) * 0.8f;
							mainHandDamage = (mainHandAttack.getExactCurrentWithoutFixedBonus() + totalBaseDamage * offHandAttack.getFixedBonusRate()) * 0.8f;
							offHandDamage = (offHandAttack.getExactCurrentWithoutFixedBonus() + totalBaseDamage * mainHandAttack.getFixedBonusRate()) * 0.8f * p.getGameStats().getSkillEfficiency();
						}
					} else {
						if (Rnd.nextInt(1000) >= p.getGameStats().getMaxDamageChance()) {
							offHandDamage *= p.getGameStats().getMinDamageRatio();
							if (offHandDamage <= 0 && offWeaponStats != null) {
								offHandDamage = 1;
							}
						}
					}
					attackResultList.add(new AttackResult(mainHandDamage, status, hitType));
					if (offWeaponStats != null)
						attackResultList.add(new AttackResult(offHandDamage, AttackStatus.getOffHandStats(status), hitType));
				}
			} else { // Attack without weapon
				// "no weapon" damage has a power of 70, whereas weapons have their own power
				// TODO: parse values, but for now we can ignore it since most player weapons have a power of 100
				float damage = Rnd.get(16, 20) * (1 + ((p.getGameStats().getPower().getCurrent() - 100) / 100f * 70f) / 100f) + mainHandAttack.getBonus();
				attackResultList.add(new AttackResult(damage, status, hitType));
			}
		} else {
			int val = attacker instanceof Homing ? 100 : Rnd.get(80, 120);
			attackResultList.add(new AttackResult(mainHandAttack.getCurrent() * val / 100f, status, hitType));
		}
		return attackResultList;
	}

	/**
	 * Applies elemental defense to incoming damage.
	 *
	 * <p>Elemental defense reduces damage linearly using a normalized scale (denominator) that depends on target type and level.</p>
	 *
	 * <ul>
	 *   <li><b>Players:</b> defense is normalized against a level-scaled denominator (base {@code 1300}, increasing by {@code +10} per level above
	 *   {@code 50}, using {@code min(attackerLevel, defenderLevel)}).</li>
	 *   <li><b>NPCs and other creatures:</b> defense is normalized against a fixed denominator of {@code 1300}.</li>
	 * </ul>
	 *
	 * <p>Damage reduction formula:</p>
	 * <pre>
	 *   finalDamage = damage * (1 - effectiveDefense / denominator)
	 * </pre>
	 *
	 * <p>Example (player(63) vs player(65), or npc(65) vs player(63)):</p>
	 * <ul>
	 *   <li>Effective level = {@code min(63, 65) = 63}</li>
	 *   <li>Denominator = {@code 1300 + (63 - 50) * 10 = 1430}</li>
	 *   <li>{@code 143} elemental defense reduces damage by {@code 10%}</li>
	 * </ul>
	 * <p>The defense value is clamped to its valid stat caps after applying movement and situational modifiers.</p>
	 *
	 * @param effector the attacking creature
	 * @param attacked the target receiving damage
	 * @param element the elemental type of the damage
	 * @param damage base damage before elemental defense
	 * @return damage reduced by elemental defense
	 */
	private static float reduceDamageByElementalDefense(Creature effector, Creature attacked, SkillElement element, float damage) {
		float elementalDenominator = getElementalDefenseDenominator(effector, attacked);
		int rawDefense = attacked.getGameStats().getElementalDefenseFor(element);
		int adjustedDefense = (int) adjustStatByMovementModifier(attacked, element.getStatForElement(), rawDefense);
		adjustedDefense = StatCapUtil.clampStatValue(element.getStatForElement(), attacked, adjustedDefense);
		return damage * (1f - adjustedDefense / elementalDenominator);
	}

	private static int getElementalDefenseDenominator(Creature effector, Creature attacked) {
		if (attacked instanceof Player) {
			int level = Math.min(effector.getLevel(), attacked.getLevel());
			return StatCapUtil.getElementalDefenseBaseValue() + Math.max(0, level - 50) * 10;
		}
		return StatCapUtil.getElementalDefenseBaseValue();
	}

	public static float calculateMagicalSkillDamage(Creature effector, Creature target, float baseDamage, int bonus, EffectTemplate template,
													boolean useMagicBoost, boolean useKnowledge, boolean useBoostSpellAttack) {
		float damage = baseDamage;
		CreatureGameStats<?> sgs = effector.getGameStats();
		CreatureGameStats<?> tgs = target.getGameStats();
		int magicBoost = 0;

		if (useMagicBoost) {
			magicBoost = sgs.getMBoost().getCurrent();
			magicBoost -= tgs.getMBResist().getCurrent();
			magicBoost = (int) Math.max(0, limit(StatEnum.BOOST_MAGICAL_SKILL, magicBoost));
		}
		int knowledge = useKnowledge ? sgs.getKnowledge().getCurrent() : 100;
		damage *= (magicBoost / 1000f) + (knowledge / 100f);

		if (useBoostSpellAttack)
			damage = sgs.getStat(StatEnum.BOOST_SPELL_ATTACK, (int) damage).getCurrent();

		// add bonus damage
		damage += bonus;
		if (template.getElement() != SkillElement.NONE && !(template instanceof NoReduceSpellATKInstantEffect)) {
			damage = reduceDamageByElementalDefense(effector, target, template.getElement(), damage);
			// damage is reduced by 100 per 1000 mdef
			damage -= adjustStatByMovementModifier(target, StatEnum.MAGICAL_DEFEND, target.getGameStats().getMDef().getCurrent()) / 10f;
		}

		if (damage < 0) {
			damage = 0;
		} else if (effector instanceof Npc && !(effector instanceof SummonedObject<?>)) {
			int rnd = (int) (damage * 0.08f);
			damage += Rnd.get(-rnd, rnd);
		}

		return damage;
	}

	/**
	 * Calculates MAGICAL CRITICAL chance
	 */
	public static boolean calculateMagicalCriticalRate(Creature attacker, Creature attacked, int criticalProb) {
		if (attacker instanceof Servant || attacker instanceof Homing)
			return false;

		float critical = attacker.getGameStats().getMCritical().getCurrent() - attacked.getGameStats().getMCR().getCurrent();
		// add critical Prob
		if (criticalProb != 100) {
			if (critical <= 0)
				critical = 1;
			critical *= criticalProb / 100f;
		}

		return Rnd.nextInt(1000) < limit(StatEnum.MAGICAL_CRITICAL, critical);
	}

	/**
	 * @return adjusted damage according to PVE or PVP modifiers
	 */
	public static float adjustDamageByPvpOrPveModifiers(Creature attacker, Creature target, float baseDamage, int pvpDamage, boolean useTemplateDmg,
		SkillElement element) {
		if (attacker.equals(target)) // e.g. material skills, which the creature casts on itself
			return baseDamage;
		int attackBonus = 0;
		int defenseBonus = 0;
		float damage = baseDamage;
		boolean pvpTarget = attacker.isPvpTarget(target);
		if (pvpTarget) {
			if (pvpDamage > 0)
				damage *= pvpDamage * 0.01f;
			damage *= 0.42f; // PVP modifier 42%, last checked on NA (4.9) 19.03.2016
			if (!useTemplateDmg) {
				attackBonus = attacker.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO, 0).getCurrent();
				defenseBonus = target.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO, 0).getCurrent();
				if (attacker.getRace() != target.getRace() && !attacker.isInInstance())
					attackBonus += Influence.getInstance().getPvpRaceBonusRatio(attacker.getRace());
				switch (element) {
					case NONE:
						attackBonus += attacker.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO_PHYSICAL, 0).getCurrent();
						defenseBonus += target.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO_PHYSICAL, 0).getCurrent();
						break;
					default:
						attackBonus += attacker.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO_MAGICAL, 0).getCurrent();
						defenseBonus += target.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO_MAGICAL, 0).getCurrent();
				}
			}
		} else if (!useTemplateDmg) {
			if (attacker instanceof Player) { // npcs dmg is not reduced because of the level difference GF (4.9) 23.04.2016
				damage *= 1f - getNpcLevelDiffMod(target, attacker);
			}
			attackBonus = attacker.getGameStats().getStat(StatEnum.PVE_ATTACK_RATIO, 0).getCurrent();
			defenseBonus = target.getGameStats().getStat(StatEnum.PVE_DEFEND_RATIO, 0).getCurrent();
			switch (element) {
				case NONE:
					attackBonus += attacker.getGameStats().getStat(StatEnum.PVE_ATTACK_RATIO_PHYSICAL, 0).getCurrent();
					defenseBonus += target.getGameStats().getStat(StatEnum.PVE_DEFEND_RATIO_PHYSICAL, 0).getCurrent();
					break;
				default:
					attackBonus += attacker.getGameStats().getStat(StatEnum.PVE_ATTACK_RATIO_MAGICAL, 0).getCurrent();
					defenseBonus += target.getGameStats().getStat(StatEnum.PVE_DEFEND_RATIO_MAGICAL, 0).getCurrent();
			}
		}
		CombatMode mode = pvpTarget ? CombatMode.PVP : CombatMode.PVE;
		// PvP/PvE ratio caps are applied after aggregation (retail behavior)
		attackBonus = StatCapUtil.limitValueForPvpOrPveStat(mode, RatioType.ATTACK, attackBonus);
		defenseBonus = StatCapUtil.limitValueForPvpOrPveStat(mode, RatioType.DEFENSE, defenseBonus);
		float multiplier = 1f + (attackBonus - defenseBonus) / 1000f;
		// Retail behavior: damage multiplier has a minimum cap of 10% (0.1f)
		multiplier = Math.max(multiplier, 0.1f);

		return damage * multiplier;
	}

	/**
	 * Must be called only once since this method triggers observe controller checks with side effects (e.g. consume one always dodge effect activation)
	 */
	public static boolean checkIsDodgedHit(Creature attacker, Creature attacked, int accMod) {
		// check if attacker is blinded
		if (attacker.getObserveController().checkAttackerStatus(AttackStatus.DODGE))
			return true;
		// check always dodge
		if (attacked.getObserveController().checkAttackStatus(AttackStatus.DODGE))
			return true;

		float accuracy = attacker.getGameStats().getMainHandPAccuracy().getCurrent() + accMod;
		float dodge = attacked.getGameStats().getEvasion().getBonus()
			+ adjustStatByMovementModifier(attacked, StatEnum.EVASION, attacked.getGameStats().getEvasion().getBase());
		float dodgeRate = dodge - accuracy;
		if (attacked instanceof Npc npc) {
			// static npcs never dodge
			if (npc.hasStatic())
				return false;
			dodgeRate *= 1 + getNpcLevelDiffMod(attacked, attacker);
		}
		return Rnd.nextInt(1000) < limit(StatEnum.EVASION, dodgeRate);
	}

	/**
	 * Must be called only once since this method triggers observe controller checks with side effects (e.g. consume one always parry effect activation)
	 */
	public static boolean checkIsParriedHit(Creature attacker, Creature attacked, int accMod) {
		// check always parry
		if (attacked.getObserveController().checkAttackStatus(AttackStatus.PARRY))
			return true;

		float accuracy = attacker.getGameStats().getMainHandPAccuracy().getCurrent() + accMod;
		float parry = attacked.getGameStats().getParry().getBonus()
			+ adjustStatByMovementModifier(attacked, StatEnum.PARRY, attacked.getGameStats().getParry().getBase());
		return Rnd.nextInt(1000) < limit(StatEnum.PARRY, parry - accuracy);
	}

	/**
	 * Must be called only once since this method triggers observe controller checks with side effects (e.g. consume one always block effect activation)
	 */
	public static boolean checkIsBlockedHit(Creature attacker, Creature attacked, int accMod) {
		// check always block
		if (attacked.getObserveController().checkAttackStatus(AttackStatus.BLOCK))
			return true;

		float accuracy = attacker.getGameStats().getMainHandPAccuracy().getCurrent() + accMod;

		float block = attacked.getGameStats().getBlock().getBonus()
			+ adjustStatByMovementModifier(attacked, StatEnum.BLOCK, attacked.getGameStats().getBlock().getBase());
		return Rnd.nextInt(1000) < limit(StatEnum.BLOCK, block - accuracy);
	}

	/**
	 * http://www.wolframalpha.com/input/?i=-0.000126341+x%5E2%2B0.184411+x-13.7738
	 * https://docs.google.com/spreadsheet/ccc?key=0AqxBGNJV9RrzdGNjbEhQNHN3S3M5bUVfUVQxRkVIT3c&hl=en_US#gid=0
	 * https://docs.google.com/spreadsheets/d/1QEET5QAnxqxgT2T82g80C_9yH2D5iFos2TadR_UqPQs/edit#gid=1008537650
	 */
	public static boolean checkIsPhysicalCriticalHit(Creature attacker, Creature attacked, boolean isMainHand, int criticalProb, boolean isSkill) {
		if (attacker instanceof Servant || attacker instanceof Homing)
			return false;
		float criticalRate;
		if (attacker instanceof Player && !isMainHand)
			criticalRate = ((PlayerGameStats) attacker.getGameStats()).getOffHandPCritical().getCurrent();
		else
			criticalRate = attacker.getGameStats().getMainHandPCritical().getCurrent();

		// check one time boost skill critical
		AttackerCriticalStatus acStatus = attacker.getObserveController().checkAttackerCriticalStatus(AttackStatus.CRITICAL, isSkill);
		if (acStatus.isResult()) {
			if (acStatus.isPercent())
				criticalRate *= (1 + acStatus.getValue() / 100);
			else
				return Rnd.nextInt(1000) < acStatus.getValue();
		}

		criticalRate -= attacked.getGameStats().getPCR().getCurrent();

		// add critical Prob
		if (criticalProb != 100 && criticalRate > 0) {
			criticalRate *= criticalProb / 100f;
		}
		return Rnd.nextInt(1000) < limit(StatEnum.PHYSICAL_CRITICAL, criticalRate);
	}

	public static int calculateMagicalResistRate(Creature attacker, Creature attacked, int accMod, SkillElement element) {
		return calculateMagicalResistRate(attacker, attacked, accMod, element, null);
	}

	public static int calculateMagicalResistRate(Creature attacker, Creature attacked, int accMod, SkillElement element,
		OneTimeBoostSkillAttack boost) {
		if (attacked.getObserveController().checkAttackStatus(AttackStatus.RESIST))
			return 1000;
		if (element != SkillElement.NONE && attacked instanceof Summon summon && element == summon.getAlwaysResistElement())
			return 1000;

		Stat2 mAccuracy = attacker.getGameStats().getMAccuracy();
		int accuracy = boost == null ? mAccuracy.getCurrent() + accMod : boost.calculateMagicalAccuracy(mAccuracy, accMod);
		int levelDiff = attacked.getLevel() - attacker.getLevel();
		int mResi = attacked.getGameStats().getMResist().getCurrent();
		int resistRate = mResi - accuracy;

		if (mResi > 0 && levelDiff > 4) // only apply if creature has mres > 0 (to keep effect of AI#modifyOwnerStat)
			resistRate += (levelDiff - 4) * 100;

		if (attacker instanceof Player && attacked instanceof Player) // checked on retail: only applies to PvP
			return Math.min(500, resistRate);

		return (int) limit(StatEnum.MAGICAL_RESIST, resistRate);
	}

	/**
	 * @return Damage of a non-lethal fall: 1% of max HP plus 4 per meter.
	 */
	public static int calculateFallDamage(Player player, float height) {
		return (int) (player.getLifeStats().getMaxHp() * height * 0.01f + height * 4);
	}

	/**
	 * Movement modifier table, indexed by move state (see {@link MovementModifierState.MoveState}) and by the column constants below.<br>
	 * The combined states are authored by hand rather than derived from their parts: forward combined with anything else loses its defense penalties,
	 * and having all three active cancels every modifier.
	 */
	private static final int[][] MOVEMENT_MODIFIERS = {
		//    atk%  def%  elem  parry  block  dodge  crit
		{        0,    0,    0,     0,     0,     0,    0 }, // 0: not moving
		{       10,  -20,  -50,     0,     0,     0,    0 }, // 1: forward
		{      -20,    0,    0,   500,   500,     0,    0 }, // 2: backward
		{       10,    0,    0,   500,   500,     0,    0 }, // 3: forward + backward
		{      -20,    0,    0,     0,     0,   300,    0 }, // 4: sideways
		{       10,    0,    0,     0,     0,   300,    0 }, // 5: forward + sideways
		{      -20,    0,    0,   500,   500,   300,    0 }, // 6: backward + sideways
		{        0,    0,    0,     0,     0,     0,    0 }, // 7: forward + backward + sideways
	};

	private static final int COLUMN_ATTACK = 0, COLUMN_DEFENSE = 1, COLUMN_ELEMENTAL_DEFENSE = 2, COLUMN_PARRY = 3, COLUMN_BLOCK = 4,
		COLUMN_DODGE = 5;

	public static float adjustStatByMovementModifier(Creature creature, StatEnum stat, float value) {
		if (stat == null || !(creature instanceof Player player))
			return value;
		int moveState = player.getMoveController().getMoveState();
		if (moveState == MovementModifierState.MoveState.NONE)
			return value;
		return adjustStatByMoveState(moveState, stat, value);
	}

	static float adjustStatByMoveState(int moveState, StatEnum stat, float value) {
		int[] modifiers = MOVEMENT_MODIFIERS[moveState];
		return switch (stat) {
			case PHYSICAL_ATTACK, MAGICAL_ATTACK -> value * (1 + modifiers[COLUMN_ATTACK] / 100f);
			case PHYSICAL_DEFENSE, MAGICAL_DEFEND -> value * (1 + modifiers[COLUMN_DEFENSE] / 100f);
			case FIRE_RESISTANCE, EARTH_RESISTANCE, WATER_RESISTANCE, WIND_RESISTANCE, LIGHT_RESISTANCE, DARK_RESISTANCE ->
				value + modifiers[COLUMN_ELEMENTAL_DEFENSE];
			case PARRY -> value + modifiers[COLUMN_PARRY];
			case BLOCK -> value + modifiers[COLUMN_BLOCK];
			case EVASION -> value + modifiers[COLUMN_DODGE];
			default -> value;
		};
	}

	/**
	 * Movement speed is not part of the modifier table above. It stays a plain per direction factor and is applied instantly, without the activation
	 * and deactivation delays of the combat modifiers.
	 */
	public static float adjustSpeedByMovementModifier(MovementModifierDirection movementDirection, float speed) {
		return switch (movementDirection) {
			case SIDEWAYS -> speed * 0.8f;
			case BACKWARD -> speed * 0.6f;
			default -> speed;
		};
	}

	private static float getNpcLevelDiffMod(Creature target, Creature attacker) {
		int levelDiff = target.getLevel() - attacker.getLevel();
		if (levelDiff <= 2)
			return 0f;
		if (levelDiff >= 12)
			return 1f;
		return (levelDiff - 2) * 0.1f;
	}

	/**
	 * @return		the smaller of {@code value} and {@code differenceLimit} for this StatEnum
	 */
	public static float limit(StatEnum statEnum, float value) {
		return Math.min(StatCapUtil.getDifferenceLimit(statEnum), value);
	}

}
