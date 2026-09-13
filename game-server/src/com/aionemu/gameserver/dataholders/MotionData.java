package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.model.*;

/**
 * @author kecimis
 */
@XmlRootElement(name = "motion_times")
@XmlAccessorType(XmlAccessType.FIELD)
public class MotionData {

	private static final String AUTO_ATTACK_MOTION = "cattack";

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
		// always the first variant, since the client reports its hit point even when a later charge stage plays another animation
		Times times = motionTime.getTimesFor(player, 1);
		if (times == null)
			return 0f;
		int motionSpeed = skill.getSkillTemplate().getMotion().getSpeed() * 10;
		float attackRate = player.getGameStats().getAttackSpeedRate();
		float motionSpeedRate = player.isHitTimeBoosted() ? Math.min(attackRate, calculateCastSpeedRate(player.getHitTimeBoostCastSpeed())) : attackRate;
		// in a mech the client reports the whole animation instead of its hit point, even though the animation carries one
		return (player.isInRobotMode() ? times.getAnimationLength() : times.getMinTime()) * motionSpeed * motionSpeedRate;
	}

	/**
	 * @return The latest point of the animation at which the client can still report a hit, or zero if the skill has no animation. Hit points beyond
	 *         the animation length exist, so the later of both bounds the corridor.
	 */
	public float calculateMaxAnimationTime(Player player, Skill skill) {
		MotionTime motionTime = getMotionTime(skill);
		if (motionTime == null)
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
