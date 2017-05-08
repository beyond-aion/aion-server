package com.aionemu.gameserver.services.vortex;

import java.util.LinkedHashMap;
import java.util.Map;

import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team.group.PlayerGroupService;
import com.aionemu.gameserver.model.vortex.VortexLocation;
import com.aionemu.gameserver.model.vortex.VortexStateType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Source
 */
public class Invasion extends DimensionalVortex<VortexLocation> {

	private final Map<Integer, Player> invaders = new LinkedHashMap<>();
	private final Map<Integer, Player> defenders = new LinkedHashMap<>();
	private PlayerAlliance invAlliance, defAlliance;

	public Invasion(VortexLocation vortex) {
		super(vortex);
	}

	@Override
	public void startInvasion() {
		getVortexLocation().setActiveVortex(this);
		despawn();
		spawn(VortexStateType.INVASION);
		initRiftGenerator();
		updateAlliance();
	}

	@Override
	public void stopInvasion() {
		getVortexLocation().setActiveVortex(null);
		unregisterSiegeBossListeners();
		for (Kisk kisk : getVortexLocation().getInvadersKisks().values()) {
			kisk.getController().die();
		}
		for (Player invader : invaders.values()) {
			if (invader.isOnline()) {
				kickPlayer(invader, true);
			}
		}
		despawn();
		spawn(VortexStateType.PEACE);
	}

	@Override
	public void addPlayer(Player player, boolean isInvader) {
		Map<Integer, Player> list = isInvader ? invaders : defenders;
		PlayerAlliance alliance = isInvader ? invAlliance : defAlliance;

		if (alliance != null && !alliance.isDisbanded()) {
			PlayerAllianceService.addPlayer(alliance, player);
		} else if (!list.isEmpty()) {
			Player first = null;

			for (Player firstOne : list.values()) {
				if (firstOne.isInGroup()) {
					PlayerGroupService.removePlayer(firstOne);
				} else if (firstOne.isInAlliance()) {
					PlayerAllianceService.removePlayer(firstOne);
				}
				first = firstOne;
			}

			if (player.equals(first)) {
				if (isInvader) {
					invAlliance = PlayerAllianceService.createAlliance(first, player, TeamType.ALLIANCE_OFFENCE);
				} else {
					defAlliance = PlayerAllianceService.createAlliance(first, player, TeamType.ALLIANCE_DEFENCE);
				}
			} else {
				kickPlayer(player, isInvader);
			}
		}
		list.put(player.getObjectId(), player);
	}

	@Override
	public void kickPlayer(Player player, boolean isInvader) {
		Map<Integer, Player> list = isInvader ? invaders : defenders;
		PlayerAlliance alliance = isInvader ? invAlliance : defAlliance;

		list.remove(player.getObjectId());

		if (alliance != null && alliance.hasMember(player.getObjectId())) {
			if (player.isOnline()) {
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(isInvader ? 1401452 : 1401476));
			}
			PlayerAllianceService.removePlayer(player);
			if (alliance.isDisbanded()) {
				if (isInvader) {
					invAlliance = null;
				} else {
					defAlliance = null;
				}
			}
		}

		if (isInvader && player.isOnline() && player.getWorldId() == getVortexLocation().getInvasionWorldId()) {
			// You will be returned to where you entered.
			PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1401474));
			TeleportService.teleportTo(player, getVortexLocation().getHomePoint());
		}

		getVortexLocation().getVortexController().getPassedPlayers().remove(player.getObjectId());
		getVortexLocation().getVortexController().syncPassed(true);
	}

	@Override
	public void updateDefenders(Player defender) {
		if (defenders.containsKey(defender.getObjectId())) {
			return;
		}

		if (defAlliance == null || !defAlliance.isFull()) {
			RequestResponseHandler<Player> responseHandler = new RequestResponseHandler<Player>(defender) {

				@Override
				public void acceptRequest(Player requester, Player responder) {
					if (responder.isInGroup()) {
						PlayerGroupService.removePlayer(responder);
					} else if (responder.isInAlliance()) {
						PlayerAllianceService.removePlayer(responder);
					}

					if (defAlliance == null || !defAlliance.isFull()) {
						addPlayer(responder, false);
					}
				}

			};

			boolean requested = defender.getResponseRequester().putRequest(904306, responseHandler);
			if (requested) {
				PacketSendUtility.sendPacket(defender, new SM_QUESTION_WINDOW(904306, 0, 0));
			}
		}
	}

	@Override
	public void updateInvaders(Player invader) {
		if (invaders.containsKey(invader.getObjectId())) {
			return;
		}

		addPlayer(invader, true);
	}

	private void updateAlliance() {
		for (Player player : getVortexLocation().getPlayers().values()) {
			if (player.getRace().equals(getVortexLocation().getDefendersRace())) {
				updateDefenders(player);
			}
		}
	}

	@Override
	public Map<Integer, Player> getInvaders() {
		return invaders;
	}

	@Override
	public Map<Integer, Player> getDefenders() {
		return defenders;
	}

}
