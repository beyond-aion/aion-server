package com.aionemu.gameserver.skillengine.properties;

import java.util.List;

import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.model.actions.PlayerMode;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.Trap;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureVisualState;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.model.templates.zone.ZoneType;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer, Yeats, Neon
 */
public class TargetRangeProperty {

	public static boolean set(Properties properties, Properties.ValidationResult result, Creature skillEffector, SkillTemplate skillTemplate, float x,
		float y, float z) {
		TargetRangeAttribute value = properties.getTargetType();
		int effectiveRange = skillEffector instanceof Trap ? skillEffector.getGameStats().getAttackRange().getCurrent() : properties.getEffectiveRange();

		final List<Creature> effectedList = result.getTargets();
		switch (value) {
			case ONLYONE:
				break;
			case AREA:
				int altitude = properties.getEffectiveAltitude() != 0 ? properties.getEffectiveAltitude() : 1;
				Creature firstTarget = result.getFirstTarget();

				if (firstTarget == null)
					return false;

				// Create a sorted map of the objects in knownlist
				// and filter them properly
				firstTarget.getKnownList().stream()
					.filter(knownObject -> knownObject.get() instanceof Creature)
					.map(knownObject -> (Creature) knownObject.get())
					.filter(creature -> checkCommonRequirements(creature, skillTemplate))
//					.filter(creature -> !(creature instanceof Kisk && isInsideDisablePvpZone(creature)))
					.filter(creature -> Math.abs(firstTarget.getZ() - creature.getZ()) <= altitude)
					.filter(creature -> !(creature instanceof Player player && player.isInPlayerMode(PlayerMode.WINDSTREAM)))
					.filter(creature -> !(skillEffector instanceof Trap trap && trap.getCreator() == creature)) // TODO this is a temporary hack for traps
					.filter(creature -> checkRange(properties, skillEffector, x, y, z, creature, effectiveRange, firstTarget))
					.filter(creature -> checkGeo(creature, result.getFirstTarget(), skillTemplate))
					.forEach(effectedList::add);
				break;
			case PARTY:
			case PARTY_WITHPET:
				// if only firsttarget will be affected (e.g. Bodyguard), we don't need to evaluate the whole group
				if (properties.getTargetMaxCount() == 1 && properties.getFirstTarget() != FirstTargetAttribute.POINT)
					break;
				if (skillEffector instanceof Player effector) {
					TemporaryPlayerTeam<? extends TeamMember<Player>> team;
					if (value == TargetRangeAttribute.PARTY_WITHPET) {
						team = effector.getCurrentTeam(); // group or whole alliance
						tryAddSummon(effector.getSummon(), result, skillTemplate, effectedList);
					} else {
						team = effector.getCurrentGroup(); // group or alliance group (max 6 targets)
					}
					if (team != null) {
						effectedList.clear();
						for (Player member : team.getMembers()) {
							if (!member.isOnline())
								continue;
							if (!checkCommonRequirements(member, skillTemplate))
								continue;
							if (PositionUtil.isInRange(effector, member, effectiveRange, false)) {
								if (checkGeo(member, result.getFirstTarget(), skillTemplate))
									effectedList.add(member);
								if (value == TargetRangeAttribute.PARTY_WITHPET)
									tryAddSummon(member.getSummon(), result, skillTemplate, effectedList);
							}
						}
					}
				}
				break;
			case POINT:
				skillEffector.getKnownList().stream()
					.filter(knownObject -> knownObject.get() instanceof Creature)
					.map(knownObject -> (Creature) knownObject.get())
					.filter(creature -> checkCommonRequirements(creature, skillTemplate))
					.filter(creature -> !(creature instanceof Trap trap) || trap.getMaster().isEnemy(skillEffector))
					.filter(creature -> PositionUtil.isInRange(creature, x, y, z, properties.getTargetDistance() + 1))
					.filter(creature -> checkGeo(creature, result.getFirstTarget(), skillTemplate))
					.forEach(effectedList::add);
				break;
		}

		return true;
	}

	private static boolean checkCommonRequirements(Creature creature, SkillTemplate skillTemplate) {
		if (skillTemplate.hasResurrectEffect()) {
			if (!creature.isDead())
				return false;
		} else {
			if (creature.isDead())
				return false;
		}

		// blinking state means protection is active (no interaction with creature is possible)
		if (creature.isInVisualState(CreatureVisualState.BLINKING))
			return false;

		return true;
	}

	@SuppressWarnings("unused")
	private static boolean isInsideDisablePvpZone(Creature creature) {
		if (creature.isInsideZoneType(ZoneType.PVP)) {
			for (ZoneInstance zone : creature.findZones()) {
				if (zone.getZoneTemplate().getFlags() == 0)
					return true;
			}
		}
		return false;
	}

	private static boolean checkRange(Properties properties, Creature skillEffector, float x, float y, float z, Creature creature, int effectiveRange, Creature firstTarget) {
		if (properties.getFirstTarget() == FirstTargetAttribute.POINT)
			return PositionUtil.isInRange(x, y, z, creature.getX(), creature.getY(), creature.getZ(), effectiveRange);
		if (properties.getIneffectiveRange() > 0 && PositionUtil.isInRange(firstTarget, creature, properties.getIneffectiveRange(), false))
			return false;
		if (properties.getEffectiveDist() > 0) {
			if (properties.getEffectiveAngle() > 0) {
				if (creature.equals(skillEffector))
					return false;
				// for target_range_area_type = firestorm
				if (properties.getEffectiveAngle() < 360) {
					float angle = properties.getEffectiveAngle() / 2f; // e.g. 60 degrees (always positive) = 30 degrees in positive and negative direction
					if (properties.getDirection() == AreaDirections.BACK) {
						if (!PositionUtil.isBehind(creature, skillEffector, angle))
							return false;
					} else if (!PositionUtil.isInFrontOf(creature, skillEffector, angle)) {
						return false;
					}
				}
				return PositionUtil.isInRange(skillEffector, creature, properties.getEffectiveDist(), false);
			} else {
				// Lightning bolt
				return PositionUtil.isInsideAttackCylinder(skillEffector, creature, properties.getEffectiveDist(), (effectiveRange / 2f), properties.getDirection());
			}
		}
		return PositionUtil.isInRange(firstTarget, creature, effectiveRange, false);
	}

	private static boolean checkGeo(VisibleObject object, Creature firstTarget, SkillTemplate skillTemplate) {
		// If creature is at least 2 meters above the terrain, ground skill cannot be applied
		if (GeoDataConfig.GEO_ENABLE) {
			if (skillTemplate.isGroundSkill()) {
				float geoZ = GeoService.getInstance().getZ(object, object.getZ() + 2, object.getZ() - 2);
				if (Float.isNaN(geoZ))
					return false;
			}
			if (skillTemplate.getProperties().getFirstTarget() != FirstTargetAttribute.POINT && !GeoService.getInstance().canSee(firstTarget, object))
				return false;
		}
		return true;
	}

	private static void tryAddSummon(Summon summon, Properties.ValidationResult result, SkillTemplate skillTemplate, List<Creature> effectedList) {
		if (summon != null && checkGeo(summon, result.getFirstTarget(), skillTemplate))
			effectedList.add(summon);
	}
}
