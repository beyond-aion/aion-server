package com.aionemu.gameserver.skillengine.effect;

import java.util.concurrent.ScheduledFuture;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.event.AIEventType;
import com.aionemu.gameserver.ai.manager.EmoteManager;
import com.aionemu.gameserver.configs.main.GeoDataConfig;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
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

		effected.getMoveController().abortMove();

		if (effected instanceof Npc) {
			EmoteManager.emoteStartAttacking((Npc) effected, effector); // set weapon_equipped for faster walk speed
			effected.getAi().setStateIfNot(AIState.FEAR);
		} else if (effected instanceof Player && effected.isInState(CreatureState.WALK_MODE)) {
			effected.unsetState(CreatureState.WALK_MODE);
			PacketSendUtility.broadcastPacket((Player) effected, new SM_EMOTION(effected, EmotionType.RUN), true);
		}
		if (GeoDataConfig.FEAR_ENABLE) {
			ScheduledFuture<?> fearTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new FearTask(effector, effected), 0, 1000);
			effect.setPeriodicTask(fearTask, position);
		}

		// resistchance of fear effect to damage, if value is lower than 100, fear can be interrupted bz damage
		// example skillId: 540 Terrible howl
		if (resistchance < 100) {
			effect.addObserver(effected, new ActionObserver(ObserverType.ATTACKED) {

				@Override
				public void attacked(Creature creature, int skillId) {
					if (Rnd.chance() >= resistchance)
						effected.getEffectController().removeEffect(effect.getSkillId());
				}
			});
		}

		if (effect.getEffected() instanceof Player player && effect.getEffector().getMaster() instanceof Player) {
			long duration = getDuration2() + ((long) getDuration1()) * effect.getSkillLevel();
			if (getRandomTime() > 0 )
				duration -= getRandomTime()/2;
			player.incrementFearCountAndUpdateExpirationTime(duration);
		}
	}

	@Override
	public void endEffect(Effect effect) {
		effect.getEffected().getEffectController().unsetAbnormal(AbnormalState.FEAR);

		effect.getEffected().getMoveController().abortMove();
		PacketSendUtility.broadcastPacketAndReceive(effect.getEffected(), new SM_TARGET_IMMOBILIZE(effect.getEffected()));

		if (effect.getEffected() instanceof Npc) {
			effect.getEffected().getAi().setStateIfNot(AIState.IDLE);
			effect.getEffected().getAi().onCreatureEvent(AIEventType.ATTACK, effect.getEffected());
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
			if (effected.getEffectController().isUnderFear() && PositionUtil.isInRange(effected, effector, 40)) {
				float angle = PositionUtil.calculateAngleFrom(effector, effected);
				float maxDistance = effected.getGameStats().getMovementSpeedFloat();
				Vector3f closestCollision = GeoService.getInstance().findMovementCollision(effected, angle, maxDistance);
				if (effected instanceof Npc) {
					((Npc) effected).getMoveController().resetMove();
					((Npc) effected).getMoveController().moveToPoint(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ());
				} else {
					byte moveAwayHeading = PositionUtil.convertAngleToHeading(angle);
					effected.getMoveController().setNewDirection(closestCollision.getX(), closestCollision.getY(), closestCollision.getZ(), moveAwayHeading);
					effected.getMoveController().startMovingToDestination();
				}
			}
		}
	}
}
