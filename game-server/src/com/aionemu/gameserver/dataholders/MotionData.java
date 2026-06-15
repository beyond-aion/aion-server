package com.aionemu.gameserver.dataholders;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.Unmarshaller;
import javax.xml.bind.annotation.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.calc.Stat2;
import com.aionemu.gameserver.skillengine.model.*;

/**
 * @author kecimis
 */
@XmlRootElement(name = "motion_times")
@XmlAccessorType(XmlAccessType.FIELD)
public class MotionData {

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
		Times times = motionTime.getTimesFor(player, 1);
		if (times == null)
			return 0f;
		int motionSpeed = skill.getSkillTemplate().getMotion().getSpeed() * 10;
		float attackRate = getAttackRate(player);
		float motionSpeedRate = player.isHitTimeBoosted() ? Math.min(attackRate, calculateCastSpeedRate(player.getHitTimeBoostCastSpeed())) : attackRate;
		return (player.isInRobotMode() ? times.getAnimationLength() : times.getMinTime()) * motionSpeed * motionSpeedRate;
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
		float attackRate = getAttackRate(player);
		float motionSpeedRate = skill.allowAnimationBoostByCastSpeed() ? Math.min(attackRate, calculateCastSpeedRate(skill.getCastSpeedForAnimationBoostAndChargeSkills())) : attackRate;
		// TODO parse movement related times (see "_run" suffix in client templates)
		int animationLastHitMillis = (int) (times.getMaxTime() * motionSpeed * motionSpeedRate);
		// TODO parse animation_length to remove approxAnimationLength (currently only parsed for AT)
		float approxAnimationLength = times.getAnimationLength() > 0 ? times.getAnimationLength() : 1.4f + times.getMaxTime();
		int animationFullDurationMillis = (int) (approxAnimationLength * motionSpeed * motionSpeedRate);
		return new AnimationTimes(animationLastHitMillis, animationFullDurationMillis);
	}

	private float getAttackRate(Player player) {
		Stat2 attackSpeedStat = player.getGameStats().getAttackSpeed();
		return attackSpeedStat.getCurrent() / (float) attackSpeedStat.getBase();
	}

	private float calculateCastSpeedRate(float castSpeedForAnimationBoost) {
		castSpeedForAnimationBoost = Math.max(0.5f, Math.min(1f, castSpeedForAnimationBoost)); // these are limits enforced by the game client
		return castSpeedForAnimationBoost + (1 - castSpeedForAnimationBoost) / 2; // only half of the cast speed can affect animations
	}

	public int size() {
		return motionTimesMap.size();
	}

	public record AnimationTimes(int lastHitMillis, int fullDurationMillis) {}
}
