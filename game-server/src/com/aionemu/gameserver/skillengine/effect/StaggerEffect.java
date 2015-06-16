package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillMoveType;
import com.aionemu.gameserver.skillengine.model.SpellStatus;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "StaggerEffect")
public class StaggerEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill();
		if (effected instanceof Player)
			((Player) effected).getFlyController().onStopGliding();
		effected.getEffectController().removeParalyzeEffects();
		World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading());
		PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_FORCED_MOVE(effect.getEffector(), effect.getEffected()
			.getObjectId(), effect.getTargetX(), effect.getTargetY(), effect.getTargetZ()));
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.STAGGER.getId());
		effect.setAbnormal(AbnormalState.STAGGER.getId());
	}

	@Override
	public void calculate(Effect effect) {
		if (effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.PULLED)
			|| effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STAGGER))
			return;

		if (!super.calculate(effect, StatEnum.STAGGER_RESISTANCE, SpellStatus.STAGGER))
			return;

		// Check for packets if this must be fixed someway, but for now it works good so
		effect.setSkillMoveType(SkillMoveType.STAGGER);
		final Creature effector = effect.getEffector();
		final Creature effected = effect.getEffected();
		// Move effected 3 meters backward as on retail
		double radian = Math.toRadians(MathUtil.convertHeadingToDegree(effector.getHeading()));
		float x1 = (float) (Math.cos(radian) * 3);
		float y1 = (float) (Math.sin(radian) * 3);

		float z = effected.getZ();
		byte intentions = (byte) (CollisionIntention.PHYSICAL.getId() | CollisionIntention.DOOR.getId());
		Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effected, effected.getX() + x1, effected.getY() + y1, z,
			false, intentions);
		float zAfterColl = closestCollision.z;
		x1 = closestCollision.x;
		y1 = closestCollision.y;
		if (Math.abs(z - zAfterColl) > 0.1f && !effected.getMoveController().isJumping()) {
			x1 = effected.getX();
			y1 = effected.getY();
			zAfterColl = z;
		}
		effect.setTargetLoc(x1, y1, zAfterColl);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.STAGGER.getId());
	}
}
