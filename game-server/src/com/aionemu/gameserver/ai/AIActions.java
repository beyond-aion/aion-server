package com.aionemu.gameserver.ai;

import java.util.Collection;

import com.aionemu.gameserver.controllers.observer.DialogObserver;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.services.RespawnService;
import com.aionemu.gameserver.services.drop.DropRegistrationService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * Here will be placed some common AI actions. These methods have access to AI's owner
 * 
 * @author ATracer
 */
public class AIActions {

	/**
	 * Despawn and delete owner
	 */
	public static void deleteOwner(AbstractAI ai2) {
		ai2.getOwner().getController().delete();
	}

	/**
	 * Target will die with all notifications using ai's owner as the last attacker
	 */
	public static void kill(AbstractAI ai2, Creature target) {
		target.getController().onDie(ai2.getOwner());
	}

	/**
	 * AI's owner will die
	 */
	public static void die(AbstractAI ai2) {
		ai2.getOwner().getController().onDie(ai2.getOwner());
	}

	/**
	 * AI's owner will die from specified attacker
	 */
	public static void die(AbstractAI ai2, Creature attacker) {
		ai2.getOwner().getController().onDie(attacker);
	}

	/**
	 * Use skill or add intention to use (will be implemented later)
	 */
	public static void useSkill(AbstractAI ai2, int skillId, int level) {
		ai2.getOwner().getController().useSkill(skillId, level);
	}

	public static void useSkill(AbstractAI ai2, int skillId) {
		ai2.getOwner().getController().useSkill(skillId);
	}

	/**
	 * Effect will be created and applied to target with 100% success
	 */
	public static void applyEffect(AbstractAI ai2, SkillTemplate template, Creature target) {
		Effect effect = new Effect(ai2.getOwner(), target, template, template.getLvl(), 0);
		effect.setIsForcedEffect(true);
		effect.initialize();
		effect.applyEffect();
	}

	public static void targetSelf(AbstractAI ai2) {
		ai2.getOwner().setTarget(ai2.getOwner());
	}

	public static void targetCreature(AbstractAI ai2, Creature target) {
		ai2.getOwner().setTarget(target);
	}

	public static void handleUseItemFinish(AbstractAI ai2, Player player) {
		ai2.getPosition().getWorldMapInstance().getInstanceHandler().handleUseItemFinish(player, ((Npc) ai2.getOwner()));
	}

	public static void fireNpcKillInstanceEvent(AbstractAI ai2, Player player) {
		ai2.getPosition().getWorldMapInstance().getInstanceHandler().onDie((Npc) ai2.getOwner());
	}

	public static void registerDrop(AbstractAI ai2, Player player, Collection<Player> registeredPlayers) {
		DropRegistrationService.getInstance().registerDrop((Npc) ai2.getOwner(), player, registeredPlayers);
	}

	public static void scheduleRespawn(AbstractAI ai2) {
		RespawnService.scheduleRespawnTask(ai2.getOwner());
	}

	/**
	 * Add RequestResponseHandler to player with senderId equal to objectId of AI owner
	 */
	public static void addRequest(AbstractAI ai2, Player player, int requestId, AIRequest request, Object... requestParams) {
		addRequest(ai2, player, requestId, ai2.getObjectId(), request, requestParams);
	}

	/**
	 * Add RequestResponseHandler to player, which cancels request on movement
	 */
	public static void addRequest(AbstractAI ai2, Player player, int requestId, int senderId, int range, final AIRequest request,
		Object... requestParams) {

		boolean requested = player.getResponseRequester().putRequest(requestId, new RequestResponseHandler<Creature>(ai2.getOwner()) {

			@Override
			public void denyRequest(Creature requester, Player responder) {
				request.denyRequest(requester, responder);
			}

			@Override
			public void acceptRequest(Creature requester, Player responder) {
				request.acceptRequest(requester, responder, requestId);
			}
		});

		if (requested) {
			if (range > 0) {
				player.getObserveController().addObserver(new DialogObserver(ai2.getOwner(), player, range) {

					@Override
					public void tooFar() {
						request.denyRequest(requester, responder);
					}
				});
			}
			PacketSendUtility.sendPacket(player, new SM_QUESTION_WINDOW(requestId, senderId, range, requestParams));
		}
	}

	/**
	 * Add RequestResponseHandler to player
	 */
	public static void addRequest(AbstractAI ai2, Player player, int requestId, int senderId, final AIRequest request, Object... requestParams) {
		addRequest(ai2, player, requestId, senderId, 0, request, requestParams);
	}
}
