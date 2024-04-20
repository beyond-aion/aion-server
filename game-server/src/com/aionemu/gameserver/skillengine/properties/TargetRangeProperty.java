package com.aionemu.gameserver.skillengine.properties;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

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
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer, Yeats, Neon
 */
public class TargetRangeProperty {

	private static final Logger log = LoggerFactory.getLogger(TargetRangeProperty.class);

	public static boolean set(final Skill skill, Properties properties) {
		Creature skillEffector = skill.getEffector();
		TargetRangeAttribute value = properties.getTargetType();
		int distanceToTarget = properties.getTargetDistance();
		int effectiveRange = skillEffector instanceof Trap ? skillEffector.getGameStats().getAttackRange().getCurrent() : properties.getEffectiveRange();
		int ineffectiveRange = properties.getIneffectiveRange();

		final List<Creature> effectedList = skill.getEffectedList();
		skill.setTargetRangeAttribute(value);
		switch (value) {
			case ONLYONE:
				break;
			case AREA:
				int altitude = properties.getEffectiveAltitude() != 0 ? properties.getEffectiveAltitude() : 1;
				final Creature firstTarget = skill.getFirstTarget();

				if (firstTarget == null) {
					log.warn("CHECKPOINT: first target is null for skillid " + skill.getSkillTemplate().getSkillId());
					return false;
				}

				// Create a sorted map of the objects in knownlist
				// and filter them properly
				for (VisibleObject nextCreature : firstTarget.getKnownList().getKnownObjects().values()) {
					if (!(nextCreature instanceof Creature))
						continue;
					Creature creature = (Creature) nextCreature;
					if (!checkCommonRequirements(creature, skill))
						continue;

					// if (nextCreature instanceof Kisk && isInsideDisablePvpZone(creature)
					// continue;

					if (Math.abs(firstTarget.getZ() - nextCreature.getZ()) > altitude
						|| ((nextCreature instanceof Player) && ((Player) nextCreature).isInPlayerMode(PlayerMode.WINDSTREAM))) {
						continue;
					}

					// TODO this is a temporary hack for traps
					if (skillEffector instanceof Trap && ((Trap) skillEffector).getCreator() == nextCreature)
						continue;

					if (skill.isPointSkill()) {
						if (PositionUtil.isInRange(skill.getX(), skill.getY(), skill.getZ(), nextCreature.getX(), nextCreature.getY(), nextCreature.getZ(),
							effectiveRange)) {
							skill.getEffectedList().add(creature);
						}
					} else if (properties.getEffectiveAngle() > 0) {
						if (nextCreature.equals(skillEffector))
							continue;
						// for target_range_area_type = firestorm
						if (properties.getEffectiveAngle() < 360) {
							float angle = properties.getEffectiveAngle() / 2f; // e.g. 60 degrees (always positive) = 30 degrees in positive and negative direction
							if (properties.getDirection() == AreaDirections.BACK) {
								if (!PositionUtil.isBehind(nextCreature, skillEffector, angle))
									continue;
							} else {
								if (!PositionUtil.isInFrontOf(nextCreature, skillEffector, angle))
									continue;
							}
						}
						if (!PositionUtil.isInRange(skillEffector, nextCreature, properties.getEffectiveDist(), false))
							continue;
						if (!skill.shouldAffectTarget(nextCreature))
							continue;
						skill.getEffectedList().add(creature);
					} else if (properties.getEffectiveDist() > 0) {
						// Lightning bolt
						if (PositionUtil.isInsideAttackCylinder(skillEffector, nextCreature, properties.getEffectiveDist(), (effectiveRange / 2f), properties.getDirection())) {
							if (!skill.shouldAffectTarget(nextCreature))
								continue;
							skill.getEffectedList().add(creature);
						}
					} else if (PositionUtil.isInRange(firstTarget, nextCreature, effectiveRange, false)) {
						// for target_range_area_type = fireball
						if (ineffectiveRange > 0 && PositionUtil.isInRange(firstTarget, nextCreature, ineffectiveRange, false))
							continue;
						if (!skill.shouldAffectTarget(nextCreature))
							continue;
						skill.getEffectedList().add(creature);
					}
				}

				break;
			case PARTY:
			case PARTY_WITHPET:
				// if only firsttarget will be affected (e.g. Bodyguard), we don't need to evaluate the whole group
				if (properties.getTargetMaxCount() == 1 && properties.getFirstTarget() != FirstTargetAttribute.POINT)
					break;
				if (skillEffector instanceof Player) {
					final Player effector = (Player) skillEffector;
					TemporaryPlayerTeam<? extends TeamMember<Player>> team;
					if (value == TargetRangeAttribute.PARTY_WITHPET) {
						team = effector.getCurrentTeam(); // group or whole alliance
					} else {
						team = effector.getCurrentGroup(); // group or alliance group (max 6 targets)
					}
					if (team != null) {
						effectedList.clear();
						for (Player member : team.getMembers()) {
							if (!member.isOnline())
								continue;
							if (!checkCommonRequirements(member, skill))
								continue;
							if (PositionUtil.isInRange(effector, member, effectiveRange, false)) {
								if (skill.shouldAffectTarget(member))
									effectedList.add(member);
								if (value == TargetRangeAttribute.PARTY_WITHPET) {
									Summon aMemberSummon = member.getSummon();
									if (aMemberSummon != null) {
										if (skill.shouldAffectTarget(aMemberSummon))
											effectedList.add(aMemberSummon);
									}
								}
							}
						}
					}
				}
				break;
			case POINT:
				for (VisibleObject nextCreature : skillEffector.getKnownList().getKnownObjects().values()) {
					if (!(nextCreature instanceof Creature))
						continue;
					Creature creature = (Creature) nextCreature;
					if (!checkCommonRequirements(creature, skill))
						continue;

					if (nextCreature instanceof Trap && !((Trap) nextCreature).getMaster().isEnemy(skillEffector))
						continue;

					if (!PositionUtil.isInRange(nextCreature, skill.getX(), skill.getY(), skill.getZ(), distanceToTarget + 1))
						continue;
					if (skill.shouldAffectTarget(nextCreature))
						effectedList.add(creature);
				}
				break;
		}

		return true;
	}

	private static boolean checkCommonRequirements(Creature creature, Skill skill) {
		if (skill.getSkillTemplate().hasResurrectEffect()) {
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
}
