package com.aionemu.gameserver.dataholders;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.commons.utils.Rnd;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.skillengine.model.*;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author kecimis
 */
@XmlRootElement(name = "motion_times")
@XmlAccessorType(XmlAccessType.FIELD)
public class MotionData {

	private static final String AUTO_ATTACK_MOTION = "cattack";
	/** Hit time of a skill whose motion has no animation for the player */
	private static final int MISSING_MOTION_MILLIS = 300;
	/** Animation length of an NPC skill whose motion has no animation for the NPC */
	private static final int MISSING_NPC_ANIMATION_MILLIS = 800;

	@XmlElement(name = "motion_time")
	private List<MotionTime> motionTimes;

	@XmlTransient
	private final Map<String, MotionTime> motionTimesMap = new HashMap<>();

	void afterUnmarshal(Unmarshaller u, Object parent) {
		for (MotionTime motion : motionTimes) {
			motionTimesMap.put(motion.getName(), motion);
		}
		motionTimes = null;
	}

	public Collection<MotionTime> getMotionTimes() {
		return motionTimesMap.values();
	}

	public MotionTime getMotionTime(String name) {
		return motionTimesMap.get(name);
	}

	public MotionTime getMotionTime(Skill skill) {
		Motion motion = skill.getSkillTemplate().getMotion();
		if (motion == null || motion.getName() == null) // instant skills like Remove Shock (283) or Feint (912)
			return null; // some skills, like Blind Side (3467) or scroll/food buffs have no motion
		return getMotionTime(motion.getName());
	}

	public float calculateAnimationTimeUntilFirstHit(Player player, Skill skill) {
		MotionTime motionTime = getMotionTime(skill);
		if (motionTime == null)
			return 0f;
		int motionSpeed = skill.getSkillTemplate().getMotion().getSpeed() * 10;
		float attackRate = player.getGameStats().getAttackSpeedRate();
		float motionSpeedRate = player.isHitTimeBoosted() ? Math.min(attackRate, calculateCastSpeedRate(player.getHitTimeBoostCastSpeed())) : attackRate;
		if (!motionTime.hasOwnAnimation(player)) // the client plays no animation for this weapon and reports none, but empty hands still wait the default
			return isUnarmed(player) ? MISSING_MOTION_MILLIS / 1000f * motionSpeed * motionSpeedRate : 0f;
		// always the first variant, since the client reports its hit point even when a later charge stage plays another animation
		Times times = motionTime.getTimesFor(player, 1);
		if (times == null)
			return 0f;
		// in a mech the client reports the whole animation instead of its hit point, even though the animation carries one
		return (player.isInRobotMode() ? times.getAnimationLength() : times.getMinTime()) * motionSpeed * motionSpeedRate;
	}

	/**
	 * @return True if the player casts the skill with empty hands, which have no animation of their own for its motion. The hit time the client
	 *         reports is then raised to the default without an audit entry, since an honest client reports zero.
	 */
	public boolean isUnarmedWithoutAnimation(Player player, Skill skill) {
		MotionTime motionTime = getMotionTime(skill);
		return motionTime != null && isUnarmed(player) && !motionTime.hasOwnAnimation(player);
	}

	private static boolean isUnarmed(Player player) {
		return !player.isInRobotMode() && player.getEquipment().getMainHandWeaponType() == null && player.getEquipment().getOffHandWeaponType() == null;
	}

	/**
	 * @return The latest hit time, before the flight of a projectile, that a client may report for a skill of an NPC it controls (summons,
	 *         mercenaries): the motion delay plus the later of the hit point and the animation length on the NPC's model, 800 ms long without one
	 */
	public int calculateClientControlledNpcMaxHitTime(int npcId, Skill skill) {
		Motion motion = skill.getSkillTemplate().getMotion();
		MotionTime motionTime = getMotionTime(skill);
		int[] hitTimes = motionTime == null ? null : motionTime.getNpcHitTimesMillis(npcId);
		Integer length = motionTime == null ? null : motionTime.getNpcAnimationLengthMillis(npcId);
		int animationMillis = Math.max(hitTimes == null ? 0 : hitTimes[0], length == null ? MISSING_NPC_ANIMATION_MILLIS : length);
		return (motion == null ? 0 : motion.getDelay()) + animationMillis;
	}

	/**
	 * @return The latest point of the animation at which the client can still report a hit, or zero if the skill has no animation. Hit points beyond
	 *         the animation length exist, so the later of both bounds the corridor.
	 */
	public float calculateMaxAnimationTime(Player player, Skill skill) {
		MotionTime motionTime = getMotionTime(skill);
		if (motionTime == null)
			return 0f;
		if (!motionTime.hasOwnAnimation(player))
			return 0f;
		float longest = 0f;
		for (int motionId = 1; motionId <= 4; motionId++) { // the client can play any variant of the motion
			Times times = motionTime.getTimesFor(player, motionId);
			if (times != null)
				longest = Math.max(longest, Math.max(times.getMaxTime(), times.getAnimationLength()));
		}
		if (longest == 0f)
			return 0f;
		int motionSpeed = skill.getSkillTemplate().getMotion().getSpeed() * 10;
		return longest * motionSpeed * player.getGameStats().getAttackSpeedRate(); // cast speed only ever shortens animations, so it stays out of the upper bound
	}

	public AnimationTimes calculateAnimationTimesAfterLastHit(Player player, Skill skill) {
		MotionTime motionTime = getMotionTime(skill);
		if (motionTime == null)
			return null;
		int motionId = skill instanceof ChargeSkill chargeSkill ? chargeSkill.getMotionId() : Math.max(1, skill.getMultiCastCount());
		Times times = motionTime.getTimesFor(player, motionId);
		if (times == null)
			return null;
		int motionSpeed = skill.getSkillTemplate().getMotion().getSpeed() * 10;
		float attackRate = player.getGameStats().getAttackSpeedRate();
		float motionSpeedRate = skill.allowAnimationBoostByCastSpeed() ? Math.min(attackRate, calculateCastSpeedRate(skill.getCastSpeedForAnimationBoostAndChargeSkills())) : attackRate;
		int animationFirstHitMillis = (int) (times.getMinTime() * motionSpeed * motionSpeedRate);
		int animationLastHitMillis = (int) (times.getMaxTime() * motionSpeed * motionSpeedRate);
		int animationFullDurationMillis = (int) (times.getAnimationLength() * motionSpeed * motionSpeedRate);
		return new AnimationTimes(animationFirstHitMillis, animationLastHitMillis, animationFullDurationMillis);
	}

	/**
	 * @return Milliseconds from the end of an NPC's cast to the hit of its skill, before the flight of a projectile: the motion delay plus the hit
	 *         point on the NPC's model, or the default hit time if the model has no such animation. Neither attack nor motion speed scale it.
	 */
	public int calculateNpcHitTime(Npc npc, Skill skill) {
		Motion motion = skill.getSkillTemplate().getMotion();
		MotionTime motionTime = getMotionTime(skill);
		int[] hitTimes = motionTime == null ? null : motionTime.getNpcHitTimesMillis(npc.getNpcId());
		return (motion == null ? 0 : motion.getDelay()) + (hitTimes == null ? MISSING_MOTION_MILLIS : hitTimes[0]);
	}

	/**
	 * @return Milliseconds until an auto attack of the NPC hits the target: the hit point of a random attack animation of its model, or the default
	 *         hit time without one, plus the flight of its projectile. Attack speed does not scale it.
	 */
	public int calculateNpcAutoAttackHitTime(Npc npc, Creature target) {
		MotionTime motionTime = getMotionTime(AUTO_ATTACK_MOTION);
		int[] hitTimes = motionTime == null ? null : motionTime.getNpcHitTimesMillis(npc.getNpcId());
		int hitTime = hitTimes == null ? MISSING_MOTION_MILLIS : hitTimes[Rnd.nextInt(hitTimes.length)];
		int ammoSpeed = npc.getObjectTemplate().getAmmoSpeed();
		if (ammoSpeed > 0)
			hitTime += (int) (PositionUtil.getDistance(npc, target) / ammoSpeed * 1000);
		return hitTime;
	}

	/**
	 * @return Milliseconds from the end of an NPC's cast until its skill animation lets it act again: the animation length on its model minus the
	 *         motion delay, 800 ms long without an animation, and 2 s when nothing is left of it. The next action also waits for the attack delay.
	 */
	public int calculateNpcSkillRecoveryMillis(Npc npc, Skill skill) {
		Motion motion = skill.getSkillTemplate().getMotion();
		MotionTime motionTime = getMotionTime(skill);
		Integer length = motionTime == null ? null : motionTime.getNpcAnimationLengthMillis(npc.getNpcId());
		int recovery = (length == null ? MISSING_NPC_ANIMATION_MILLIS : length) - (motion == null ? 0 : motion.getDelay());
		return recovery > 0 ? recovery : 2000;
	}

	/**
	 * @return Milliseconds after an auto attack of the NPC until its attack animation lets it act again: the latest hit point of all its attack
	 *         animations, scaled by its attack speed. The next action also waits for the attack delay.
	 */
	public int calculateNpcAutoAttackRecoveryMillis(Npc npc) {
		MotionTime motionTime = getMotionTime(AUTO_ATTACK_MOTION);
		int[] hitTimes = motionTime == null ? null : motionTime.getNpcHitTimesMillis(npc.getNpcId());
		int latestHit = hitTimes == null ? MISSING_MOTION_MILLIS : Arrays.stream(hitTimes).max().getAsInt();
		Stat2 attackSpeed = npc.getGameStats().getAttackSpeed();
		return attackSpeed.getBase() > 0 ? Math.round(latestHit * attackSpeed.getCurrent() / (float) attackSpeed.getBase()) : latestHit;
	}

	/**
	 * @return How long a cast whose skill has no hit animation for the player holds the next skill and auto attack, the default hit time scaled
	 *         like a real one
	 */
	public int calculateMissingMotionMillis(Player player, Skill skill) {
		Motion motion = skill.getSkillTemplate().getMotion();
		int motionSpeed = motion == null ? 100 : motion.getSpeed();
		float attackRate = player.getGameStats().getAttackSpeedRate();
		float motionSpeedRate = skill.allowAnimationBoostByCastSpeed() ? Math.min(attackRate, calculateCastSpeedRate(skill.getCastSpeedForAnimationBoostAndChargeSkills())) : attackRate;
		return Math.round(MISSING_MOTION_MILLIS * motionSpeed / 100f * motionSpeedRate);
	}

	/**
	 * @return The range of hit times the client can report for an auto attack, or null if there is no attack animation for the players weapon (in
	 *         robot mode there is none, and the client sends zero)
	 */
	public AttackHitTimes calculateAutoAttackHitTimes(Player player) {
		MotionTime motionTime = getMotionTime(AUTO_ATTACK_MOTION);
		if (motionTime == null)
			return null;
		Times firstVariant = motionTime.getTimesFor(player, 1);
		if (firstVariant == null)
			return null;
		Times secondVariant = motionTime.getTimesFor(player, 2); // the client picks either variant, so both are legal
		float attackRate = player.getGameStats().getAttackSpeedRate();
		int first = Math.round(firstVariant.getMinTime() * 1000 * attackRate);
		int second = secondVariant == null ? first : Math.round(secondVariant.getMinTime() * 1000 * attackRate);
		return new AttackHitTimes(first, second);
	}

	private float calculateCastSpeedRate(float castSpeedForAnimationBoost) {
		float rate = castSpeedForAnimationBoost + (1 - castSpeedForAnimationBoost) / 2; // only half of the cast speed can affect animations
		return Math.clamp(rate, 0.75f, 1.5f); // these are limits enforced by the game client, and they cap the result, not the input
	}

	public int size() {
		return motionTimesMap.size();
	}

	public record AnimationTimes(int firstHitMillis, int lastHitMillis, int fullDurationMillis) {}

	public record AttackHitTimes(int firstVariantMillis, int secondVariantMillis) {

		public int lowestMillis() {
			return Math.min(firstVariantMillis, secondVariantMillis);
		}

		public int highestMillis() {
			return Math.max(firstVariantMillis, secondVariantMillis);
		}

		public int millisFor(int variant) {
			return variant == 2 ? secondVariantMillis : firstVariantMillis;
		}
	}
}
