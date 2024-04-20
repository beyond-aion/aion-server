package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SubEffectType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Sarynth, Wakizashi, Sippolo
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "PulledEffect")
public class PulledEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		EffectController ec = effect.getEffected().getEffectController();
		if (ec.isAbnormalSet(AbnormalState.PULLED) || ec.isAbnormalSet(AbnormalState.STUMBLE) || ec.isAbnormalSet(AbnormalState.OPENAERIAL))
			return;

		if (!GeoService.getInstance().canSee(effect.getEffected(),effect.getEffector())) {
			return;
		}
		if (!super.calculate(effect, StatEnum.PULLED_RESISTANCE, null))
			return;
		if (effect.isSubEffect())
			effect.setSubEffectType(effect.getEffected() instanceof Player ? SubEffectType.PULL : SubEffectType.PULL_NPC);
		final Creature effector = effect.isReflected() ? effect.getOriginalEffected() : effect.getEffector();
		// Target must be pulled just one meter away from effector, not IN place of effector
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(PositionUtil.getHeadingTowards(effector, effect.getEffected())));
		float z = effector.getZ();
		final float x1 = (float) Math.cos(radian) * 1.5f;
		final float y1 = (float) Math.sin(radian) * 1.5f;
		Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effect.getEffected(),effector.getX() + x1, effector.getY() + y1, z);
		effect.setTargetLoc(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ());
	}

	@Override
	public void startEffect(Effect effect) {
		Creature effected = effect.getEffected();
		if (!effect.isReflected()) {
			effected.getController().cancelCurrentSkill(effect.getEffector());
			if (effected instanceof Player player) {
				player.getFlyController().onStopGliding();
				player.getController().onStopMove();
			}
		}
		World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading());
		if (effected instanceof Player)
			PacketSendUtility.broadcastPacketAndReceive(effected,
					new SM_FORCED_MOVE(effect.isReflected() ? effect.getOriginalEffected() : effect.getEffector(), effected.getObjectId(), effect.getTargetX(),
							effect.getTargetY(), effect.getTargetZ()));
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.PULLED);
		effect.setAbnormal(AbnormalState.PULLED);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.PULLED);
	}
}
