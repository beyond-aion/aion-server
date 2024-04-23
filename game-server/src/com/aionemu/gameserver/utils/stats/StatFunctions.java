package com.aionemu.gameserver.utils.stats;

import java.util.ArrayList;
import java.util.List;

import org.apache.commons.lang3.ArrayUtils;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.configs.main.FallDamageConfig;
import com.aionemu.gameserver.configs.main.RatesConfig;
import com.aionemu.gameserver.controllers.attack.AttackResult;
import com.aionemu.gameserver.controllers.attack.AttackStatus;
import com.aionemu.gameserver.controllers.observer.AttackerCriticalStatus;
import com.aionemu.gameserver.model.SkillElement;
import com.aionemu.gameserver.model.gameobjects.*;
import com.aionemu.gameserver.model.gameobjects.player.Equipment;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.Rates;
import com.aionemu.gameserver.model.siege.Influence;
import com.aionemu.gameserver.model.stats.calc.AdditionStat;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.model.stats.calc.StatCapUtil;
import com.aionemu.gameserver.model.stats.container.CreatureGameStats;
import com.aionemu.gameserver.model.stats.container.PlayerGameStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.WeaponStats;
import com.aionemu.gameserver.model.templates.item.enums.ItemSubType;
import com.aionemu.gameserver.model.templates.npc.NpcRating;
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
	 * @param maxLevelInRange
	 *          - level of the player who receives the reward (solo) or max player level in range (group)
	 * @param target
	 *          - the npc
	 * @return XP reward from target
	 */
	public static long calculateExperienceReward(int maxLevelInRange, Npc target) {
		WorldMapInstance instance = target.getPosition().getWorldMapInstance();
		int baseXP = calculateBaseExp(target);
		float mapMulti = instance.getInstanceHandler().getInstanceExpMultiplier(); // map modifier to approach retail exp values
		if (instance.getParent().isInstanceType() && instance.getMaxPlayers() >= 2 && instance.getMaxPlayers() <= 6) {
			mapMulti *= instance.getMaxPlayers(); // on retail you get mob EP * max instance member count (only for group instances)
			mapMulti /= RatesConfig.XP_SOLO_RATES[0]; // custom: divide by regular xp rates, so they will not affect the rewarded XP
		}
		int xpPercentage = XPRewardEnum.xpRewardFrom(target.getLevel() - maxLevelInRange);
		long rewardXP = Math.round(baseXP * mapMulti * (xpPercentage / 100f));
		return rewardXP;
	}

	/**
	 * @return Experience value identical to the ones seen on aion databases (but seen retail exp rewards are always higher)
	 */
	private static int calculateBaseExp(Npc npc) {
		int maxHp = npc.getObjectTemplate().getStatsTemplate().getMaxHp();
		if (maxHp <= 0)
			return 0;
		float multiplier;
		switch (npc.getRating()) {
			case JUNK:
				multiplier = 2f;
				break;
			case NORMAL:
				multiplier = 2.2f;
				break;
			case ELITE:
				multiplier = 4f;
				break;
			case HERO:
				multiplier = 5.4f;
				break;
			case LEGENDARY:
				multiplier = 6.4f;
				break;
			default:
				throw new IllegalArgumentException("Could not calculate experience reward for " + npc + " due to unknown rating.");
		}
		multiplier += npc.getRank().ordinal() * 0.2f;
		return Math.round(maxHp * multiplier);
	}

	/**
	 * @return DP reward from target
	 */
	public static int calculateDPReward(Player player, Creature target) {
		int playerLevel = player.getCommonData().getLevel();
		int targetLevel = target.getLevel();
		NpcRating npcRating = ((Npc) target).getObjectTemplate().getRating();

		// TODO: fix to see monster Rating level, NORMAL lvl 1, 2 | ELITE lvl 1, 2 etc..
		// look at:
		// http://www.aionsource.com/forum/mechanic-analysis/42597-character-stats-xp-dp-origin-gerbator-team-july-2009-a.html
		int baseDP = targetLevel * calculateRatingMultiplier(npcRating);
		int xpPercentage = XPRewardEnum.xpRewardFrom(targetLevel - playerLevel);
		return (int) Rates.DP_PVE.calcResult(player, (int) Math.floor(baseDP * xpPercentage / 100f));
	}

	/**
	 * @return AP reward
	 */
	public static int calculatePvEApGained(Player player, Creature target) {
		if (player.getCommonData().getLevel() - target.getLevel() > 10)
			return 1;

		float apNpcRate = getApNpcRating(((Npc) target).getObjectTemplate().getRating());

		// TODO: find out why they give 1/4 AP base(normal NpcRate) (5 AP retail)
		if (target.getName().equals("flame hoverstone"))
			apNpcRate = 0.5f;

		return (int) Rates.AP_PVE.calcResult(player, (int) Math.floor(15 * apNpcRate));
	}

	/**
	 * @return Points Lost in PvP Death
	 */
	public static int calculatePvPApLost(Player defeated, Player winner) {
		int pointsLost = defeated.getAbyssRank().getRank().getPointsLost();

		// Level penalty calculation
		int difference = winner.getLevel() - defeated.getLevel();

		if (difference >= 5)
			pointsLost = Math.round(pointsLost * 0.1f);
		else if (difference == 4)
			pointsLost = Math.round(pointsLost * 0.65f);
		else if (difference == 3)
			pointsLost = Math.round(pointsLost * 0.85f);

		return (int) Rates.AP_PVP_LOST.calcResult(defeated, pointsLost);
	}

	/**
	 * @return Points Gained in PvP Kill
	 */
	public static int calculatePvpApGained(Player defeated, int winnerAbyssRank, int maxLevel) {
		int pointsGained = defeated.getAbyssRank().getRank().getPointsGained();

		// Level penalty calculation
		int difference = maxLevel - defeated.getLevel();

		if (difference > 4) {
			pointsGained = Math.round(pointsGained * 0.1f);
		} else if (difference < -3) {
			pointsGained = Math.round(pointsGained * 1.3f);
		} else {
			switch (difference) {
				case 3:
					pointsGained = Math.round(pointsGained * 0.85f);
					break;
				case 4:
					pointsGained = Math.round(pointsGained * 0.65f);
					break;
				case -2:
					pointsGained = Math.round(pointsGained * 1.1f);
					break;
				case -3:
					pointsGained = Math.round(pointsGained * 1.2f);
					break;
			}
		}

		// Abyss rank penalty calculation
		int defeatedAbyssRank = defeated.getAbyssRank().getRank().getId();
		int abyssRankDifference = winnerAbyssRank - defeatedAbyssRank;

		if (winnerAbyssRank <= 7 && abyssRankDifference > 0) {
			float penaltyPercent = abyssRankDifference * 0.05f;

			pointsGained -= Math.round(pointsGained * penaltyPercent);
		}

		return pointsGained;
	}

	/**
	 * @return XP Points Gained in PvP Kill TODO: Find the correct formula.
	 */
	public static int calculatePvpXpGained(Player defeated, int winnerAbyssRank, int maxLevel) {
		int pointsGained = 5000;

		// Level penalty calculation
		int difference = maxLevel - defeated.getLevel();

		if (difference > 4) {
			pointsGained = Math.round(pointsGained * 0.1f);
		} else if (difference < -3) {
			pointsGained = Math.round(pointsGained * 1.3f);
		} else {
			switch (difference) {
				case 3:
					pointsGained = Math.round(pointsGained * 0.85f);
					break;
				case 4:
					pointsGained = Math.round(pointsGained * 0.65f);
					break;
				case -2:
					pointsGained = Math.round(pointsGained * 1.1f);
					break;
				case -3:
					pointsGained = Math.round(pointsGained * 1.2f);
					break;
			}
		}

		// Abyss rank penalty calculation
		int defeatedAbyssRank = defeated.getAbyssRank().getRank().getId();
		int abyssRankDifference = winnerAbyssRank - defeatedAbyssRank;

		if (winnerAbyssRank <= 7 && abyssRankDifference > 0) {
			float penaltyPercent = abyssRankDifference * 0.05f;

			pointsGained -= Math.round(pointsGained * penaltyPercent);
		}

		return pointsGained;
	}

	public static int calculatePvpDpGained(Player defeated, int maxRank, int maxLevel) {
		int pointsGained;

		// base values
		int baseDp = 1064;
		int dpPerRank = 57;

		// adjust by rank
		pointsGained = (defeated.getAbyssRank().getRank().getId() - maxRank) * dpPerRank + baseDp;

		// adjust by level
		pointsGained = StatFunctions.adjustPvpDpGained(pointsGained, defeated.getLevel(), maxLevel);

		return pointsGained;
	}

	@SuppressWarnings("lossy-conversions")
	public static int adjustPvpDpGained(int points, int defeatedLvl, int killerLvl) {
		int pointsGained = points;

		int difference = killerLvl - defeatedLvl;
		// adjust by level
		if (difference >= 10)
			pointsGained = 0;
		else if (difference >= 0)
			pointsGained -= pointsGained * difference * 0.1;
		else if (difference <= -10)
			pointsGained *= 1.1;
		else
			pointsGained += pointsGained * Math.abs(difference) * 0.01;

		return pointsGained;
	}

	/**
	 * Hate based on BOOST_HATE stat Now used only from skills, probably need to use for regular attack
	 */
	public static int calculateHate(Creature creature, int value) {
		Stat2 stat = new AdditionStat(StatEnum.BOOST_HATE, value, creature, 0.1f);
		return creature.getGameStats().getStat(StatEnum.BOOST_HATE, stat).getCurrent();
	}

	public static List<AttackResult> calculateAttackDamage(Creature attacker,
														   SkillElement element, AttackStatus status, CalculationType... calculationTypes) {
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
					if (ArrayUtils.contains(calculationTypes, CalculationType.SKILL)) { // 80% of damage is added on retail
						if (offWeaponStats != null) {
							float totalBaseDamage = (offHandAttack.getExactBaseWithoutBaseRate() * p.getGameStats().getSkillEfficiency() + mainHandAttack.getExactBaseWithoutBaseRate()) * 0.8f;
							mainHandDamage = (mainHandAttack.getExactCurrentWithoutFixedBonus() + totalBaseDamage * offHandAttack.getFixedBonusRate()) * 0.8f;
							offHandDamage = (offHandAttack.getExactCurrentWithoutFixedBonus() + totalBaseDamage * mainHandAttack.getFixedBonusRate()) * 0.8f * p.getGameStats().getSkillEfficiency();
						}
					} else {
						if (Rnd.get(1000) >= p.getGameStats().getMaxDamageChance()) {
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
	 * elemental resistance, 145 = 10% magical damage reduction (cap at +-1150)
	 * @return damage reduced by elemental resistance
	 */
	private static float reduceDamageByElementalResistance(Creature attacked, SkillElement element, float damage) {
		return damage * (1 - getMovementModifier(attacked, SkillElement.getResistanceForElement(element), attacked.getGameStats().getMagicalDefenseFor(element))/ 1450f);
	}


	public static float calculateMagicalSkillDamage(Creature speller, Creature target, float baseDamage, int bonus, SkillElement element,
													boolean useMagicBoost, boolean useKnowledge) {
		CreatureGameStats<?> sgs = speller.getGameStats();
		CreatureGameStats<?> tgs = target.getGameStats();

		float magicBoost = useMagicBoost ? sgs.getMBoost().getCurrent() : 0;
		magicBoost -= speller instanceof Trap ? 0 : tgs.getMBResist().getCurrent();

		magicBoost = (int) Math.max(0, limit(StatEnum.BOOST_MAGICAL_SKILL, magicBoost));
		float knowledge = useKnowledge ? sgs.getKnowledge().getCurrent() : 100; // this line might be wrong now
		float damage = baseDamage * (1 + (magicBoost / (knowledge * 10)));


		damage = sgs.getStat(StatEnum.BOOST_SPELL_ATTACK, (int) damage).getCurrent();
		// add bonus damage
		damage += bonus;
		if (element != SkillElement.NONE) {
			damage = reduceDamageByElementalResistance(target, element, damage);
			// damage is reduced by 100 per 1000 mdef
			damage -= target.getGameStats().getMDef().getCurrent()/10f;
		}

		if (damage < 0) {
			damage = 0;
		} else if (speller instanceof Npc && !(speller instanceof SummonedObject<?>)) {
			int rnd = (int) (damage * 0.08f);
			damage += Rnd.get(-rnd, rnd);
		}

		return damage;
	}

	/**
	 * Calculates MAGICAL CRITICAL chance
	 */
	public static boolean calculateMagicalCriticalRate(Creature attacker, Creature attacked, int criticalProb, boolean applyMcrit) {
		if (attacker instanceof Servant || attacker instanceof Homing || !applyMcrit)
			return false;

		float critical = attacker.getGameStats().getMCritical().getCurrent() - attacked.getGameStats().getMCR().getCurrent();
		// add critical Prob
		if (criticalProb != 100) {
			if (critical <= 0)
				critical = 1;
			critical *= criticalProb / 100f;
		}

		return Rnd.get(1000) < limit(StatEnum.MAGICAL_CRITICAL, critical);
	}

	public static int calculateRatingMultiplier(NpcRating npcRating) {
		// FIXME: to correct formula, have any reference?
		switch (npcRating) {
			case JUNK:
			case NORMAL:
				return 2;
			case ELITE:
				return 3;
			case HERO:
				return 4;
			case LEGENDARY:
				return 5;
			default:
				return 1;
		}
	}

	public static int getApNpcRating(NpcRating npcRating) {
		switch (npcRating) {
			case JUNK:
				return 1;
			case NORMAL:
				return 2;
			case ELITE:
				return 4;
			case HERO:
				return 35;// need check
			case LEGENDARY:
				return 2500;// need check
			default:
				return 1;
		}
	}

	/**
	 * @return adjusted damage according to PVE or PVP modifiers
	 */
	public static float adjustDamageByPvpOrPveModifiers(Creature attacker, Creature target, float baseDamage, int pvpDamage, boolean useTemplateDmg,
		SkillElement element) {
		float attackBonus = 1;
		float defenseBonus = 1;
		float damage = baseDamage;
		if (attacker.isPvpTarget(target)) {
			if (pvpDamage > 0)
				damage *= pvpDamage * 0.01f;
			damage *= 0.42f; // PVP modifier 42%, last checked on NA (4.9) 19.03.2016
			if (!useTemplateDmg) {
				if (attacker.getRace() != target.getRace() && !attacker.isInInstance())
					damage *= Influence.getInstance().getPvpRaceBonus(attacker.getRace());

				attackBonus = attacker.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO, 0).getCurrent() * 0.001f;
				defenseBonus = target.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO, 0).getCurrent() * 0.001f;
				switch (element) {
					case NONE:
						attackBonus += attacker.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO_PHYSICAL, 0).getCurrent() * 0.001f;
						defenseBonus += target.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO_PHYSICAL, 0).getCurrent() * 0.001f;
						break;
					default:
						attackBonus += attacker.getGameStats().getStat(StatEnum.PVP_ATTACK_RATIO_MAGICAL, 0).getCurrent() * 0.001f;
						defenseBonus += target.getGameStats().getStat(StatEnum.PVP_DEFEND_RATIO_MAGICAL, 0).getCurrent() * 0.001f;
				}
			}
		} else if (!useTemplateDmg) {
			if (attacker instanceof Player) {
				int levelDiff = target.getLevel() - attacker.getLevel(); // npcs dmg is not reduced because of the level difference GF (4.9) 23.04.2016
				damage *= (1f - getNpcLevelDiffMod(levelDiff, 0));
			}
			attackBonus = attacker.getGameStats().getStat(StatEnum.PVE_ATTACK_RATIO, 0).getCurrent() * 0.001f;
			defenseBonus = target.getGameStats().getStat(StatEnum.PVE_DEFEND_RATIO, 0).getCurrent() * 0.001f;
			switch (element) {
				case NONE:
					attackBonus += attacker.getGameStats().getStat(StatEnum.PVE_ATTACK_RATIO_PHYSICAL, 0).getCurrent() * 0.001f;
					defenseBonus += target.getGameStats().getStat(StatEnum.PVE_DEFEND_RATIO_PHYSICAL, 0).getCurrent() * 0.001f;
					break;
				default:
					attackBonus += attacker.getGameStats().getStat(StatEnum.PVE_ATTACK_RATIO_MAGICAL, 0).getCurrent() * 0.001f;
					defenseBonus += target.getGameStats().getStat(StatEnum.PVE_DEFEND_RATIO_MAGICAL, 0).getCurrent() * 0.001f;
			}
		}
		return damage + (damage * attackBonus) - (damage * defenseBonus);
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
			+ getMovementModifier(attacked, StatEnum.EVASION, attacked.getGameStats().getEvasion().getBase());
		float dodgeRate = dodge - accuracy;
		if (attacked instanceof Npc npc) {
			// static npcs never dodge
			if (npc.hasStatic())
				return false;
			int levelDiff = attacked.getLevel() - attacker.getLevel();
			dodgeRate *= 1 + getNpcLevelDiffMod(levelDiff, 0);
		}
		return Rnd.get(1000) < limit(StatEnum.EVASION, dodgeRate);
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
			+ getMovementModifier(attacked, StatEnum.PARRY, attacked.getGameStats().getParry().getBase());
		return Rnd.get(1000) < limit(StatEnum.PARRY, parry - accuracy);
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
			+ getMovementModifier(attacked, StatEnum.BLOCK, attacked.getGameStats().getBlock().getBase());
		return Rnd.get(1000) < limit(StatEnum.BLOCK, block - accuracy);
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
				return Rnd.get(1000) < acStatus.getValue();
		}

		criticalRate -= attacked.getGameStats().getPCR().getCurrent();

		// add critical Prob
		if (criticalProb != 100 && criticalRate > 0) {
			criticalRate *= criticalProb / 100f;
		}
		return Rnd.get(1000) < limit(StatEnum.PHYSICAL_CRITICAL, criticalRate);
	}

	public static int calculateMagicalResistRate(Creature attacker, Creature attacked, int accMod, SkillElement element) {
		if (attacked.getObserveController().checkAttackStatus(AttackStatus.RESIST))
			return 1000;
		if (element != SkillElement.NONE && attacked instanceof Summon summon && element == summon.getAlwaysResistElement())
			return 1000;

		int levelDiff = attacked.getLevel() - attacker.getLevel();
		int mResi = attacked.getGameStats().getMResist().getCurrent();
		int resistRate = mResi - attacker.getGameStats().getMAccuracy().getCurrent() - accMod;

		if (mResi > 0 && levelDiff > 4) // only apply if creature has mres > 0 (to keep effect of AI#modifyOwnerStat)
			resistRate += (levelDiff - 4) * 100;

		if (attacker instanceof Player && attacked instanceof Player) // checked on retail: only applies to PvP
			return Math.min(500, resistRate);

		return (int) limit(StatEnum.MAGICAL_RESIST, resistRate);
	}

	public static int calculateFallDamage(Player player, float distance) {
		int fallDamage = 0;
		if (distance >= FallDamageConfig.MAXIMUM_DISTANCE_DAMAGE) {
			fallDamage = player.getLifeStats().getCurrentHp();
		} else if (distance >= FallDamageConfig.MINIMUM_DISTANCE_DAMAGE) {
			float dmgPerMeter = player.getLifeStats().getMaxHp() * FallDamageConfig.FALL_DAMAGE_PERCENTAGE / 100f;
			fallDamage = (int) (distance * dmgPerMeter);
		}
		return fallDamage;
	}

	public static float getMovementModifier(Creature creature, StatEnum stat, float value) {
		if (!(creature instanceof Player) || stat == null)
			return value;

		Player player = (Player) creature;
		int h = player.getMoveController().getMovementHeading();
		if (h < 0)
			return value;
		// 7 0 1
		// \ | /
		// 6- -2
		// / | \
		// 5 4 3
		switch (h) {
			case 7:
			case 0:
			case 1:
				switch (stat) {
					case WATER_RESISTANCE:
					case WIND_RESISTANCE:
					case FIRE_RESISTANCE:
					case EARTH_RESISTANCE:
					case ELEMENTAL_RESISTANCE_DARK:
					case ELEMENTAL_RESISTANCE_LIGHT:
					case PHYSICAL_DEFENSE:
						return value * 0.8f;
				}
				break;
			case 6:
			case 2:
				switch (stat) {
					case EVASION:
						return value + 300;
					case SPEED:
						return value * 0.8f;
				}
				break;
			case 5:
			case 4:
			case 3:
				switch (stat) {
					case PARRY:
					case BLOCK:
						return value + 500;
					case SPEED:
						return value * 0.6f;
				}
				break;
		}
		return value;
	}

	public static float adjustDamageByMovementModifier(Creature creature, float value) {
		if (!(creature instanceof Player))
			return value;
		int h = ((Player) creature).getMoveController().getMovementHeading();
		if (h < 0)
			return value;
		switch (h) {
			case 7:
			case 0:
			case 1:
				value *= 1.1f;
				break;
			case 6:
			case 2:
				value *= 0.8f; // correct? it's only 30% according to https://web.archive.org/web/20170429204823/gameguide.na.aiononline.com/aion/Combat
				break;
			case 5:
			case 4:
			case 3:
				value *= 0.8f; // correct? it's only 30% according to https://web.archive.org/web/20170429204823/gameguide.na.aiononline.com/aion/Combat
				break;
		}
		return value;
	}

	private static float getNpcLevelDiffMod(int levelDiff, int base) {
		switch (levelDiff) {
			case 3:
				return 0.1f;
			case 4:
				return 0.2f;
			case 5:
				return 0.3f;
			case 6:
				return 0.4f;
			case 7:
				return 0.5f;
			case 8:
				return 0.6f;
			case 9:
				return 0.7f;
			default:
				if (levelDiff > 9)
					return 0.8f;
		}
		return base;
	}

	/**
	 * @return		the smaller of {@code value} and {@code differenceLimit} for this StatEnum
	 */
	public static float limit(StatEnum statEnum, float value) {
		return Math.min(StatCapUtil.getDifferenceLimit(statEnum), value);
	}

}
