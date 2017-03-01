package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.controllers.effect.EffectController;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillMoveType;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Sarynth modified by Wakizashi, Sippolo
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

		if (!super.calculate(effect, StatEnum.PULLED_RESISTANCE, null))
			return;

		effect.setSkillMoveType(SkillMoveType.PULL);
		final Creature effector = effect.getEffector();
		// Target must be pulled just one meter away from effector, not IN place of effector
		double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(effector.getHeading()));
		float z = effector.getZ();
		final float x1 = (float) Math.cos(radian);
		final float y1 = (float) Math.sin(radian);
		effect.setTargetLoc(effector.getX() + x1, effector.getY() + y1, z);
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill(effect.getEffector());
		if (effected instanceof Player)
			((Player) effected).getFlyController().onStopGliding();
		// effected.getMoveController().abortMove();
		World.getInstance().updatePosition(effected, effect.getTargetX(), effect.getTargetY(), effect.getTargetZ(), effected.getHeading());
		PacketSendUtility.broadcastPacketAndReceive(effected, new SM_FORCED_MOVE(effect.getEffector(), effected.getObjectId(), effect.getTargetX(),
			effect.getTargetY(), effect.getTargetZ()));
		effect.getEffected().getEffectController().setAbnormal(AbnormalState.PULLED);
		effect.setAbnormal(AbnormalState.PULLED);
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.PULLED);
	}
}
