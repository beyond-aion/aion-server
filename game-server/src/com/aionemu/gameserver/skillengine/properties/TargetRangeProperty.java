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
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author ATracer
 * @modified Yeats, Neon
 */
public class TargetRangeProperty {

	private static final Logger log = LoggerFactory.getLogger(TargetRangeProperty.class);

	/**
	 * @param skill
	 * @param properties
	 * @return
	 */
	public static final boolean set(final Skill skill, Properties properties) {
		Creature skillEffector = skill.getEffector();
		TargetRangeAttribute value = properties.getTargetType();
		int distanceToTarget = properties.getTargetDistance();
		int effectiveRange = skillEffector instanceof Trap ? skillEffector.getGameStats().getAttackRange().getCurrent() : properties.getEffectiveRange();
		int altitude = properties.getEffectiveAltitude() != 0 ? properties.getEffectiveAltitude() : 1;
		int ineffectiveRange = properties.getIneffectiveRange();

		final List<Creature> effectedList = skill.getEffectedList();
		skill.setTargetRangeAttribute(value);
		switch (value) {
			case ONLYONE:
				break;
			case AREA:
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
						if (MathUtil.isIn3dRange(skill.getX(), skill.getY(), skill.getZ(), nextCreature.getX(), nextCreature.getY(), nextCreature.getZ(),
							effectiveRange)) {
							skill.getEffectedList().add(creature);
						}
					} else if (properties.getEffectiveAngle() > 0) {
						// for target_range_area_type = firestorm
						if (properties.getEffectiveAngle() < 360) {
							float angle = properties.getEffectiveAngle() / 2f; // e.g. 60 degrees (always positive) = 30 degrees in positive and negative direction
							float angleToTarget = PositionUtil.getAngleToTarget(skillEffector, nextCreature);
							if (angleToTarget > 180) // convert 0 to 360 range => -180 to 180 range (0 is in front of effector)
								angleToTarget -= 360;
							if (properties.getDirection() != AreaDirections.BACK) {
								if (!MathUtil.isBetween(-angle, angle, angleToTarget)) // e.g. range from -30 to 30, not inside means miss
									continue;
							} else {
								angle = 180 - angle; // convert effective angle to ineffective angle
								if (MathUtil.isBetween(-angle, angle, angleToTarget)) // e.g. range from -150 to 150, inside means miss
									continue;
							}
						}
						if (!MathUtil.isInRange(skillEffector, nextCreature, properties.getEffectiveDist()))
							continue;
						if (nextCreature == skillEffector) {
							continue;
						}
						skill.getEffectedList().add(creature);
					} else if (properties.getEffectiveDist() > 0) {
						// Lightning bolt
						if (MathUtil.isInsideAttackCylinder(skillEffector, nextCreature,
							(properties.getEffectiveDist() + skillEffector.getObjectTemplate().getBoundRadius().getFront()),
							((effectiveRange / 2f) + skillEffector.getObjectTemplate().getBoundRadius().getSide()), properties.getDirection())) {
							if (!skill.shouldAffectTarget(nextCreature))
								continue;
							skill.getEffectedList().add(creature);
						}
					} else if (MathUtil.isIn3dRange(firstTarget, nextCreature,
						effectiveRange + firstTarget.getObjectTemplate().getBoundRadius().getCollision())) {
						// for target_range_area_type = fireball
						if (ineffectiveRange > 0
							&& MathUtil.isIn3dRange(firstTarget, nextCreature, ineffectiveRange + firstTarget.getObjectTemplate().getBoundRadius().getCollision()))
							continue;
						if (!skill.shouldAffectTarget(nextCreature))
							continue;
						skill.getEffectedList().add(creature);
					}
				}

				break;
			case PARTY:
			case PARTY_WITHPET:
				if (skillEffector instanceof Player) {
					final Player effector = (Player) skillEffector;
					TemporaryPlayerTeam<? extends TeamMember<Player>> team;
					if (value == TargetRangeAttribute.PARTY_WITHPET)
						team = effector.getCurrentTeam(); // group or whole alliance
					else
						team = effector.getCurrentGroup(); // group or alliance group (max 6 targets)
					if (team != null) {
						effectedList.clear();
						for (Player member : team.getMembers()) {
							if (!member.isOnline())
								continue;
							if (!checkCommonRequirements(member, skill))
								continue;
							if (MathUtil.isIn3dRange(effector, member, effectiveRange + 1)) {
								effectedList.add(member);
								if (value == TargetRangeAttribute.PARTY_WITHPET) {
									Summon aMemberSummon = member.getSummon();
									if (aMemberSummon != null)
										effectedList.add(aMemberSummon);
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

					if (MathUtil.getDistance(skill.getX(), skill.getY(), skill.getZ(), nextCreature.getX(), nextCreature.getY(),
						nextCreature.getZ()) <= distanceToTarget + 1) {
						effectedList.add(creature);
					}
				}
				break;
		}

		return true;
	}

	private static final boolean checkCommonRequirements(Creature creature, Skill skill) {
		if (skill.getSkillTemplate().hasResurrectEffect()) {
			if (!creature.getLifeStats().isAlreadyDead())
				return false;
		} else {
			if (creature.getLifeStats().isAlreadyDead())
				return false;
		}

		// blinking state means protection is active (no interaction with creature is possible)
		if (creature.isInVisualState(CreatureVisualState.BLINKING))
			return false;

		return true;
	}

	@SuppressWarnings("unused")
	private static final boolean isInsideDisablePvpZone(Creature creature) {
		if (creature.isInsideZoneType(ZoneType.PVP)) {
			for (ZoneInstance zone : creature.findZones()) {
				if (zone.getZoneTemplate().getFlags() == 0)
					return true;
			}
		}
		return false;
	}
}
