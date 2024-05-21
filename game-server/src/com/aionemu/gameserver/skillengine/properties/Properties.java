package com.aionemu.gameserver.skillengine.properties;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.effect.AbnormalState;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "Properties")
public class Properties {

	@XmlAttribute(name = "first_target", required = true)
	protected FirstTargetAttribute firstTarget;

	@XmlAttribute(name = "first_target_range")
	protected int firstTargetRange;

	@XmlAttribute(name = "awr")
	protected boolean addWeaponRange;

	@XmlAttribute(name = "target_relation")
	protected TargetRelationAttribute targetRelation;

	@XmlAttribute(name = "target_type")
	protected TargetRangeAttribute targetType;

	@XmlAttribute(name = "target_distance")
	protected int targetDistance;

	@XmlAttribute(name = "target_maxcount")
	protected int targetMaxCount;

	@XmlAttribute(name = "target_status")
	private List<AbnormalState> targetStatus;

	@XmlAttribute(name = "revision_distance")
	protected int revisionDistance;

	@XmlAttribute(name = "effective_range")
	private int effectiveRange;

	@XmlAttribute(name = "effective_altitude")
	private int effectiveAltitude;

	@XmlAttribute(name = "effective_angle")
	private int effectiveAngle;

	@XmlAttribute(name = "effective_dist")
	private int effectiveDist;

	@XmlAttribute(name = "direction")
	protected AreaDirections direction = AreaDirections.NONE;

	@XmlAttribute(name = "target_species")
	protected TargetSpeciesAttribute targetSpecies;

	@XmlAttribute(name = "ineffective_range")
	protected int ineffectiveRange;

	public boolean validate(Skill skill, CastState castState) {
		if (firstTarget != null) {
			if (!FirstTargetProperty.set(skill, this)) {
				return false;
			}
		}
		if (firstTargetRange != 0 || addWeaponRange) {
			if (!FirstTargetRangeProperty.set(skill, this, castState)) {
				return false;
			}
		}
		return validateEffectedList(skill);
	}

	public boolean endCastValidate(Skill skill) {
		Creature firstTarget = skill.getFirstTarget();
		skill.getEffectedList().clear();
		skill.getEffectedList().add(firstTarget);

		if (firstTargetRange != 0) {
			if (!FirstTargetRangeProperty.set(skill, this, CastState.CAST_END)) {
				return false;
			}
		}
		return validateEffectedList(skill);
	}

	private boolean validateEffectedList(Skill skill) {
		ValidationResult result = validateEffectedList(skill.getEffectedList(), skill.getFirstTarget(), skill.getEffector(), skill.getSkillTemplate(), skill.getX(), skill.getY(), skill.getZ());
		skill.setFirstTarget(result.getFirstTarget());
		return result.isValid();
	}

	public ValidationResult validateEffectedList(List<Creature> targets, Creature firstTarget, Creature effector, SkillTemplate skillTemplate, float x,
		float y, float z) {
		ValidationResult result = new ValidationResult(targets, firstTarget);
		if (targetType != null && !TargetRangeProperty.set(this, result, effector, skillTemplate, x, y, z))
			return result;
		if (targetRelation != null && !TargetRelationProperty.set(this, result, effector, skillTemplate))
			return result;
		if (targetStatus != null && !TargetStatusProperty.set(this, result, skillTemplate))
			return result;
		if (targetSpecies != null && !TargetSpeciesProperty.set(this, result))
			return result;
		if (targetType != null && !MaxCountProperty.set(this, result))
			return result;
		result.valid = true;
		return result;
	}

	public FirstTargetAttribute getFirstTarget() {
		return firstTarget;
	}

	public int getFirstTargetRange() {
		return firstTargetRange;
	}

	public boolean isAddWeaponRange() {
		return addWeaponRange;
	}

	public TargetRelationAttribute getTargetRelation() {
		return targetRelation;
	}

	public TargetRangeAttribute getTargetType() {
		return targetType;
	}

	public int getTargetDistance() {
		return targetDistance;
	}

	public int getTargetMaxCount() {
		return targetMaxCount;
	}

	public List<AbnormalState> getTargetStatus() {
		return targetStatus;
	}

	public int getRevisionDistance() {
		return revisionDistance;
	}

	public int getEffectiveRange() {
		return effectiveRange;
	}

	public int getEffectiveAltitude() {
		return effectiveAltitude;
	}

	public int getEffectiveDist() {
		return effectiveDist;
	}

	public int getEffectiveAngle() {
		return effectiveAngle;
	}

	public AreaDirections getDirection() {
		return direction;
	}

	public TargetSpeciesAttribute getTargetSpecies() {
		return targetSpecies;
	}

	public enum CastState {
		CAST_START,
		CAST_END;
	}

	public int getIneffectiveRange() {
		return ineffectiveRange;
	}

	public static class ValidationResult {

		private final List<Creature> targets;
		private Creature firstTarget;
		private boolean valid;

		public ValidationResult(List<Creature> targets, Creature firstTarget) {
			this.targets = targets;
			this.firstTarget = firstTarget;
		}

		public List<Creature> getTargets() {
			return targets;
		}

		public Creature getFirstTarget() {
			return firstTarget;
		}

		public void setFirstTarget(Creature firstTarget) {
			this.firstTarget = firstTarget;
		}

		public boolean isValid() {
			return valid;
		}
	}
}
