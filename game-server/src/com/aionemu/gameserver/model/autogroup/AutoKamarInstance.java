package com.aionemu.gameserver.model.autogroup;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.equalTo;

import java.util.List;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.KamarReward;
import com.aionemu.gameserver.model.team2.TeamType;
import com.aionemu.gameserver.model.team2.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.services.instance.periodic.KamarBattlefieldService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class AutoKamarInstance extends AutoInstance {

	@Override
	public AGQuestion addPlayer(Player player, SearchInstance searchInstance) {
		super.writeLock();
		try {
			if (!satisfyTime(searchInstance) || (players.size() >= agt.getPlayerSize())) {
				return AGQuestion.FAILED;
			}
			EntryRequestType ert = searchInstance.getEntryRequestType();
			List<AGPlayer> playersByRace = getAGPlayersByRace(player.getRace());
			if (ert.isGroupEntry()) {
				if (searchInstance.getMembers().size() + playersByRace.size() > 12) {
					return AGQuestion.FAILED;
				}
				for (Player member : player.getPlayerAlliance2().getOnlineMembers()) {
					if (searchInstance.getMembers().contains(member.getObjectId())) {
						players.put(member.getObjectId(), new AGPlayer(player));
					}
				}
			} else {
				if (playersByRace.size() >= 12) {
					return AGQuestion.FAILED;
				}
				players.put(player.getObjectId(), new AGPlayer(player));
			}
			return instance != null ? AGQuestion.ADDED : (players.size() == agt.getPlayerSize() ? AGQuestion.READY : AGQuestion.ADDED);
		} finally {
			super.writeUnlock();
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		super.onEnterInstance(player);
		List<Player> playersByRace = getPlayersByRace(player.getRace());
		playersByRace.remove(player);

		if (playersByRace.size() == 1 && !playersByRace.get(0).isInAlliance2()) {
			PlayerAlliance alliance = PlayerAllianceService.createAlliance(playersByRace.get(0), player, TeamType.AUTO_ALLIANCE);
			int allianceId = alliance.getObjectId();
			if (!instance.isRegistered(allianceId)) {
				instance.register(allianceId);
			}
		} else if (!playersByRace.isEmpty() && playersByRace.get(0).isInAlliance2()) {
			PlayerAllianceService.addPlayer(playersByRace.get(0).getPlayerAlliance2(), player);
		}
		Integer object = player.getObjectId();
		if (!instance.isRegistered(object)) {
			instance.register(object);
		}
		PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE((byte) 1, 27));
	}

	@Override
	public void onPressEnter(Player player) {
		super.onPressEnter(player);
		KamarBattlefieldService.getInstance().addCooldown(player);
		((KamarReward) instance.getInstanceHandler().getInstanceReward()).portToPosition(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.unregister(player);
		PlayerAllianceService.removePlayer(player);
	}

	private List<AGPlayer> getAGPlayersByRace(Race race) {
		return select(players, having(on(AGPlayer.class).getRace(), equalTo(race)));
	}

	private List<Player> getPlayersByRace(Race race) {
		return select(instance.getPlayersInside(), having(on(Player.class).getRace(), equalTo(race)));
	}

}
