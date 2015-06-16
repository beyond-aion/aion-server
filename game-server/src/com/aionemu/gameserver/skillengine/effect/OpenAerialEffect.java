package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FORCED_MOVE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SpellStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "OpenAerialEffect")
public class OpenAerialEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
	   if (effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.PULLED) 
			  || effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STUMBLE) || effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.OPENAERIAL)
			  || effect.getEffected().getEffectController().isAbnormalSet(AbnormalState.STAGGER))
		 return;
		super.calculate(effect, StatEnum.OPENAREIAL_RESISTANCE, SpellStatus.OPENAERIAL);
	}

	@Override
	public void startEffect(Effect effect) {
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill();
		effect.getEffected().getEffectController().removeParalyzeEffects();
		if (effected instanceof Player)
			 ((Player) effected).getFlyController().onStopGliding();
		PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_FORCED_MOVE(effect.getEffector(), effect.getEffected().getObjectId(), effect.getEffected().getX(), effect.getEffected().getY(), effect.getEffected().getZ()));
		World.getInstance().updatePosition(effect.getEffected(), effect.getEffected().getX(), effect.getEffected().getY(), effect.getEffected().getZ(), effect.getEffected().getHeading());
		//effected.getMoveController().abortMove();
		effected.getEffectController().setAbnormal(AbnormalState.OPENAERIAL.getId());
		effect.setAbnormal(AbnormalState.OPENAERIAL.getId());
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.OPENAERIAL.getId());
	}
}
