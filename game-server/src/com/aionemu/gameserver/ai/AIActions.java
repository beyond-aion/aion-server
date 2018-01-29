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
	public static void deleteOwner(AbstractAI<? extends Creature> ai) {
		ai.getOwner().getController().delete();
	}

	/**
	 * Target will die with all notifications using ai's owner as the last attacker
	 */
	public static void kill(AbstractAI<? extends Creature> ai, Creature target) {
		target.getController().onDie(ai.getOwner());
	}

	/**
	 * AI's owner will die
	 */
	public static void die(AbstractAI<? extends Creature> ai) {
		ai.getOwner().getController().onDie(ai.getOwner());
	}

	/**
	 * AI's owner will die from specified attacker
	 */
	public static void die(AbstractAI<? extends Creature> ai, Creature attacker) {
		ai.getOwner().getController().onDie(attacker);
	}

	/**
	 * Use skill or add intention to use (will be implemented later)
	 */
	public static void useSkill(AbstractAI<? extends Creature> ai, int skillId, int level) {
		ai.getOwner().getController().useSkill(skillId, level);
	}

	public static void useSkill(AbstractAI<? extends Creature> ai, int skillId) {
		ai.getOwner().getController().useSkill(skillId);
	}

	public static void targetSelf(AbstractAI<? extends Creature> ai) {
		ai.getOwner().setTarget(ai.getOwner());
	}

	public static void targetCreature(AbstractAI<? extends Creature> ai, Creature target) {
		ai.getOwner().setTarget(target);
	}

	public static void handleUseItemFinish(AbstractAI<? extends Creature> ai, Player player) {
		ai.getPosition().getWorldMapInstance().getInstanceHandler().handleUseItemFinish(player, ((Npc) ai.getOwner()));
	}

	public static void fireNpcKillInstanceEvent(AbstractAI<? extends Creature> ai, Player player) {
		ai.getPosition().getWorldMapInstance().getInstanceHandler().onDie((Npc) ai.getOwner());
	}

	public static void registerDrop(AbstractAI<? extends Creature> ai, Player player, Collection<Player> registeredPlayers) {
		DropRegistrationService.getInstance().registerDrop((Npc) ai.getOwner(), player, registeredPlayers);
	}

	public static void scheduleRespawn(AbstractAI<? extends Creature> ai) {
		RespawnService.scheduleRespawn(ai.getOwner());
	}

	/**
	 * Add RequestResponseHandler to player with senderId equal to objectId of AI owner
	 */
	public static void addRequest(AbstractAI<? extends Creature> ai, Player player, int requestId, AIRequest request, Object... requestParams) {
		addRequest(ai, player, requestId, ai.getObjectId(), request, requestParams);
	}

	/**
	 * Add RequestResponseHandler to player, which cancels request on movement
	 */
	public static void addRequest(AbstractAI<? extends Creature> ai, Player player, int requestId, int senderId, int range, final AIRequest request,
		Object... requestParams) {

		boolean requested = player.getResponseRequester().putRequest(requestId, new RequestResponseHandler<Creature>(ai.getOwner()) {

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
				player.getObserveController().addObserver(new DialogObserver(ai.getOwner(), player, range) {

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
	public static void addRequest(AbstractAI<? extends Creature> ai, Player player, int requestId, int senderId, final AIRequest request,
		Object... requestParams) {
		addRequest(ai, player, requestId, senderId, 0, request, requestParams);
	}
}
