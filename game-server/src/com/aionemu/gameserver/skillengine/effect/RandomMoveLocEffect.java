package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.movement.PlayerMoveController;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.skillengine.model.DashStatus;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Bio
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RandomMoveLocEffect")
public class RandomMoveLocEffect extends EffectTemplate {

	@XmlAttribute(name = "distance")
	private float distance;
	@XmlAttribute(name = "direction")
	private float direction;
	@XmlAttribute(name = "reserved5")
	private int reserved5;

	@Override
	public void applyEffect(Effect effect) {
		Skill skill = effect.getSkill();
		World.getInstance().updatePosition(effect.getEffector(), skill.getX(), skill.getY(), skill.getZ(), skill.getH());
		if (effect.getEffector().getMoveController() instanceof PlayerMoveController pmc)
			pmc.setHasMovedByRandomMoveLocEffect(skill);
	}

	@Override
	public void calculate(Effect effect) {
		effect.addSuccessEffect(this);
		DashStatus ds = reserved5 == 1 ? DashStatus.RANDOMMOVELOC_NEW : DashStatus.RANDOMMOVELOC;
		effect.setDashStatus(ds);

		Creature effector = effect.getEffector();
		// Move Effector backwards direction=1 or frontwards direction=0
		float dir = PositionUtil.convertHeadingToAngle(effector.getHeading());
		Vector3f closestCollision = GeoService.getInstance().findMovementCollision(effector, direction == 1 ? dir + 180 : dir, distance);
		effect.getSkill().setTargetPosition(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), effector.getHeading());
	}
}
