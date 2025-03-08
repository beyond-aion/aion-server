package com.aionemu.gameserver.services;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.main.InstanceConfig;
import com.aionemu.gameserver.model.DuelResult;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.SummonedObject;
import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.summons.SummonMode;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.services.summons.SummonsService;
import com.aionemu.gameserver.skillengine.model.DispelSlotType;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Simple, Sphinx, xTz
 */
public class DuelService {

	private static final Logger log = LoggerFactory.getLogger(DuelService.class);
	private final ConcurrentHashMap<Integer, Integer> duels = new ConcurrentHashMap<>();
	private final ConcurrentHashMap<Integer, Future<?>> drawTasks = new ConcurrentHashMap<>();

	public static DuelService getInstance() {
		return SingletonHolder.instance;
	}

	private DuelService() {
		log.info("DuelService started.");
	}

	/**
	 * Send the duel request to the target
	 *
	 * @param requester
	 *          the player who requested the duel
	 * @param targetPlayer
	 *          the player who respond to duel request
	 */
	public void onDuelRequest(Player requester, Player targetPlayer) {
		if (targetPlayer == null || requester.equals(targetPlayer)) {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_NO_USER_TO_REQUEST());
			return;
		}
		if (requester.isInInstance() && !InstanceConfig.INSTANCE_DUEL_ENABLE) {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_MSG_DUEL_CANT_IN_THIS_ZONE());
			return;
		}
		if (isDueling(requester)) {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_YOU_ARE_IN_DUEL_ALREADY());
			return;
		}
		if (isDueling(targetPlayer)) {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_IN_DUEL_ALREADY(targetPlayer.getName()));
			return;
		}
		if (targetPlayer.getPlayerSettings().isInDeniedStatus(DeniedStatus.DUEL)) {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_DUEL(targetPlayer.getName()));
			return;
		}
		if (requester.isDead() || targetPlayer.isDead()) {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_PARTNER_INVALID(targetPlayer.getName()));
			return;
		}
		for (ZoneInstance zone : targetPlayer.findZones()) {
			if (!zone.isOtherRaceDuelsAllowed() && !targetPlayer.getRace().equals(requester.getRace())
				|| (!zone.isSameRaceDuelsAllowed() && targetPlayer.getRace().equals(requester.getRace()))) {
				PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_MSG_DUEL_CANT_IN_THIS_ZONE());
				return;
			}
		}

		RequestResponseHandler<Player> rrh = new RequestResponseHandler<>(requester) {

			@Override
			public void denyRequest(Player requester, Player responder) {
				rejectDuelRequest(requester, responder);
			}

			@Override
			public void acceptRequest(Player requester, Player responder) {
				if (!isDueling(requester))
					startDuel(requester, responder);
			}
		};
		if (targetPlayer.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST, rrh)) {
			PacketSendUtility.sendPacket(targetPlayer,
				new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST, 0, 0, requester.getName()));
			PacketSendUtility.sendPacket(targetPlayer, SM_SYSTEM_MESSAGE.STR_DUEL_REQUESTED(requester.getName()));
			confirmDuelWith(requester, targetPlayer);
		} else {
			PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_CANT_REQUEST_WHEN_HE_IS_ASKED_QUESTION(targetPlayer.getName()));
		}
	}

	/**
	 * Asks confirmation for the duel request
	 *
	 * @param requester
	 *          the player who requested the duel
	 * @param targetPlayer
	 *          the player who the requester wants to duel with
	 */
	public void confirmDuelWith(Player requester, Player targetPlayer) {
		// Check if requester isn't already in a duel and responder is same race
		if (requester.isEnemy(targetPlayer))
			return;

		RequestResponseHandler<Player> rrh = new RequestResponseHandler<>(targetPlayer) {

			@Override
			public void acceptRequest(Player targetPlayer, Player responder) {
				cancelDuelRequest(responder, targetPlayer);
			}
		};
		requester.getResponseRequester().putRequest(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST, rrh);
		PacketSendUtility.sendPacket(requester,
			new SM_QUESTION_WINDOW(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST, 0, 0, targetPlayer.getName()));
		PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_DUEL_REQUEST_TO_PARTNER(targetPlayer.getName()));
	}

	/**
	 * Rejects the duel request
	 *
	 * @param requester
	 *          the duel requester
	 * @param responder
	 *          the duel responder
	 */
	private void rejectDuelRequest(Player requester, Player responder) {
		PacketSendUtility.sendPacket(requester, SM_CLOSE_QUESTION_WINDOW.STR_DUEL_HE_REJECT_DUEL(responder.getName()));
		PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_DUEL_REJECT_DUEL(requester.getName()));
		requester.getResponseRequester().remove(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST);
	}

	private void cancelDuelRequest(Player canceller, Player target) {
		PacketSendUtility.sendPacket(target, SM_CLOSE_QUESTION_WINDOW.STR_DUEL_REQUESTER_WITHDRAW_REQUEST(canceller.getName()));
		PacketSendUtility.sendPacket(canceller, SM_SYSTEM_MESSAGE.STR_DUEL_WITHDRAW_REQUEST(target.getName()));
		target.getResponseRequester().remove(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_ACCEPT_REQUEST);
	}

	/**
	 * Starts the duel
	 *
	 * @param requester
	 *          the player to start duel with
	 * @param responder
	 *          the other player
	 */
	private void startDuel(Player requester, Player responder) {
		if (requester.getResponseRequester().remove(SM_QUESTION_WINDOW.STR_DUEL_DO_YOU_WITHDRAW_REQUEST))
			PacketSendUtility.sendPacket(requester, SM_CLOSE_QUESTION_WINDOW.CLOSE_QUESTION_WINDOW());
		PacketSendUtility.sendPacket(requester, SM_DUEL.SM_DUEL_STARTED(responder.getObjectId()));
		PacketSendUtility.sendPacket(responder, SM_DUEL.SM_DUEL_STARTED(requester.getObjectId()));
		registerDuel(requester.getObjectId(), responder.getObjectId());
		createTask(requester, responder);
		if (requester.isInAnyHide())
			requester.getController().onHide();
		if (responder.isInAnyHide())
			responder.getController().onHide();
	}

	/**
	 * send SM_DELETE a second time to fix client not fading out the char (only happens when dueling with a team member of a group or alliance)
	 */
	public void fixTeamVisibility(Player hiddenDuelist) {
		Integer opponentId = DuelService.getInstance().getOpponentId(hiddenDuelist);
		if (opponentId != null) {
			Player opponent = World.getInstance().getPlayer(opponentId);
			if (opponent != null && opponent.getKnownList().knows(hiddenDuelist) && !opponent.getKnownList().sees(hiddenDuelist)
				&& hiddenDuelist.isInSameTeam(opponent))
				PacketSendUtility.sendPacket(opponent, new SM_DELETE(hiddenDuelist));
		}
	}

	/**
	 * Lets the given player lose the duel, ending it
	 */
	public void loseDuel(Player loser) {
		Integer opponentId = getOpponentId(loser);
		if (opponentId == null) // not dueling
			return;

		Player winner = World.getInstance().getPlayer(opponentId);
		if (winner != null) {
			winner.getEffectController().removeByDispelSlotType(DispelSlotType.DEBUFF); // all debuffs are removed from winner, but buffs will remain
			winner.getController().cancelCurrentSkill(null);
			winner.getAggroList().remove(loser);
			cancelSlaveAttacks(loser, winner);
			PacketSendUtility.sendPacket(winner, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_WON, loser.getName()));
			PacketSendUtility.sendPacket(loser, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_LOST, winner.getName()));
		} else { // duel winner is already out of world
			cancelSlaveAttacks(loser);
			PacketSendUtility.sendPacket(loser, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_LOST, null));
		}
		removeDuel(loser);
	}

	private void cancelSlaveAttacks(Player... players) {
		for (Player player : players) {
			player.getKnownList().forEachObject(visibleObject -> { // cancel summoned object attacking
				if (visibleObject instanceof SummonedObject<?> summonedObject) {
					Skill castingSkill = summonedObject.getCastingSkill();
					if (castingSkill != null && player.equals(castingSkill.getFirstTarget()) && !summonedObject.isEnemy(player))
						summonedObject.getController().cancelCurrentSkill(null);
				} else if (visibleObject instanceof Summon summon) { // cancel summon attacking
					if (summon.getMode() == SummonMode.ATTACK && player.equals(summon.getTarget()) && !summon.isEnemy(player))
						SummonsService.doMode(SummonMode.GUARD, summon);
				}
			});
		}
	}

	private void createTask(Player requester, Player responder) {
		// Schedule for draw
		Future<?> task = ThreadPoolManager.getInstance().schedule(() -> {
			if (isDueling(requester, responder)) {
				PacketSendUtility.sendPacket(requester, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_DRAW, requester.getName()));
				PacketSendUtility.sendPacket(responder, SM_DUEL.SM_DUEL_RESULT(DuelResult.DUEL_DRAW, responder.getName()));
				removeDuel(requester);
			}
		}, 5, TimeUnit.MINUTES); // 5 minutes battle retail like

		drawTasks.put(requester.getObjectId(), task);
		drawTasks.put(responder.getObjectId(), task);
	}

	public Integer getOpponentId(Player player) {
		return duels.get(player.getObjectId());
	}

	/**
	 * @return true if player is dueling
	 */
	public boolean isDueling(Player player) {
		Integer opponentId = getOpponentId(player);
		return opponentId != null && duels.get(opponentId) != null;
	}

	/**
	 * @return true if player is dueling given target
	 */
	public boolean isDueling(Player player, Player opponent) {
		Integer opponentId = getOpponentId(player);
		return opponentId != null && opponentId == opponent.getObjectId();
	}

	private void registerDuel(int requesterObjId, int responderObjId) {
		duels.put(requesterObjId, responderObjId);
		duels.put(responderObjId, requesterObjId);
	}

	private void removeDuel(Player player) {
		Integer opponentId = duels.remove(player.getObjectId());
		if (opponentId != null) {
			duels.remove(opponentId);
			removeAndEndTask(player.getObjectId());
			removeAndEndTask(opponentId);
		}
	}

	private void removeAndEndTask(int playerId) {
		Future<?> task = drawTasks.remove(playerId);
		if (task != null)
			task.cancel(false);
	}

	private static class SingletonHolder {

		protected static final DuelService instance = new DuelService();
	}
}
