package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.ScheduledFuture;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.geoEngine.collision.CollisionIntention;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_IMMOBILIZE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Sarynth
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "FearEffect")
public class FearEffect extends EffectTemplate {

	@XmlAttribute
	protected int resistchance = 100;

	@Override
	public void applyEffect(Effect effect) {
		Creature effected = effect.getEffected();
		effected.getEffectController().removeHideEffects();
		// Fear stops gliding
		if (effected instanceof Player && ((Player) effected).isInGlidingState()) {
			((Player) effected).getFlyController().onStopGliding();
		}

		effect.addToEffectedController();
	}

	@Override
	public void calculate(Effect effect) {
		super.calculate(effect, StatEnum.FEAR_RESISTANCE, null);
	}

	@Override
	public void startEffect(final Effect effect) {
		final Creature effector = effect.isReflected() ? effect.getOriginalEffected() : effect.getEffector();
		final Creature effected = effect.getEffected();
		effected.getController().cancelCurrentSkill(effector);
		effect.setAbnormal(AbnormalState.FEAR);
		effected.getEffectController().setAbnormal(AbnormalState.FEAR);

		// PacketSendUtility.broadcastPacketAndReceive(effected, new SM_TARGET_IMMOBILIZE(effected));
		effected.getMoveController().abortMove();

		if (effected instanceof Npc)
			((NpcAI) effected.getAi()).setStateIfNot(AIState.FEAR);
		if (GeoDataConfig.FEAR_ENABLE) {
			ScheduledFuture<?> fearTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FearTask(effector, effected), 0, 1000);
			effect.setPeriodicTask(fearTask, position);
		}

		// resistchance of fear effect to damage, if value is lower than 100, fear can be interrupted bz damage
		// example skillId: 540 Terrible howl
		if (resistchance < 100) {
			ActionObserver observer = new ActionObserver(ObserverType.ATTACKED) {

				@Override
				public void attacked(Creature creature, int skillId) {
					if (Rnd.chance() >= resistchance)
						effected.getEffectController().removeEffect(effect.getSkillId());
				}
			};
			effected.getObserveController().addObserver(observer);
			effect.setActionObserver(observer, position);
		}
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.FEAR);

		effect.getEffected().getMoveController().abortMove();
		PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_TARGET_IMMOBILIZE(effect.getEffected()));
		if (effect.getEffected() instanceof Npc) {
			((NpcAI) effect.getEffected().getAi()).setStateIfNot(AIState.FIGHT);
			effect.getEffected().getAi().onGeneralEvent(AIEventType.MOVE_ARRIVED);
		}

		if (resistchance < 100) {
			ActionObserver observer = effect.getActionObserver(position);
			if (observer != null)
				effect.getEffected().getObserveController().removeObserver(observer);
		}
	}

	class FearTask implements Runnable {

		private Creature effector;
		private Creature effected;

		FearTask(Creature effector, Creature effected) {
			this.effector = effector;
			this.effected = effected;
		}

		@Override
		public void run() {
			if (effected.getEffectController().isUnderFear()) {
				float x = effected.getX();
				float y = effected.getY();
				if (!PositionUtil.isInRange(effected, effector, 40))
					return;
				float angle = PositionUtil.calculateAngleFrom(effector, effected);
				byte moveAwayHeading = PositionUtil.convertAngleToHeading(angle);
				double radian = Math.toRadians(angle);
				float maxDistance = effected.getGameStats().getMovementSpeedFloat();
				float x1 = (float) (Math.cos(radian) * maxDistance);
				float y1 = (float) (Math.sin(radian) * maxDistance);
				byte intentions = (byte) (CollisionIntention.PHYSICAL.getId() | CollisionIntention.DOOR.getId());
				Vector3f closestCollision = GeoService.getInstance().getClosestCollision(effected, x + x1, y + y1, effected.getZ(), true, intentions);
				if (effected.isFlying()) {
					closestCollision.setZ(effected.getZ());
				}
				if (effected instanceof Npc) {
					((Npc) effected).getMoveController().resetMove();
					((Npc) effected).getMoveController().moveToPoint(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ());
				} else {
					effected.getMoveController().setNewDirection(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), moveAwayHeading);
					effected.getMoveController().startMovingToDestination();
				}
			}
		}
	}
}
