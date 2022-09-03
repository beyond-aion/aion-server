package com.aionemu.gameserver.model.autogroup;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamType;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.alliance.PlayerAllianceService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.services.instance.periodic.KamarBattlefieldService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xTz
 */
public class AutoKamarBattlefieldInstance extends AutoInstance {

	public AutoKamarBattlefieldInstance(AutoGroupType agt) {
		super(agt);
	}

	@Override
	public AGQuestion addPlayer(Player player, SearchInstance searchInstance) {
		super.writeLock();
		try {
			if (!satisfyTime(searchInstance) || (players.size() >= getMaxPlayers())) {
				return AGQuestion.FAILED;
			}
			EntryRequestType ert = searchInstance.getEntryRequestType();
			List<AGPlayer> playersByRace = getAGPlayersByRace(player.getRace());
			if (ert.isGroupEntry()) {
				if (searchInstance.getMembers().size() + playersByRace.size() > 12) {
					return AGQuestion.FAILED;
				}
				for (Player member : player.getPlayerAlliance().getOnlineMembers()) {
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
			return instance != null ? AGQuestion.ADDED : (players.size() == getMaxPlayers() ? AGQuestion.READY : AGQuestion.ADDED);
		} finally {
			super.writeUnlock();
		}
	}

	@Override
	public void onEnterInstance(Player player) {
		super.onEnterInstance(player);
		List<Player> playersByRace = getPlayersByRace(player.getRace());
		playersByRace.remove(player);

		if (playersByRace.size() == 1 && !playersByRace.get(0).isInAlliance()) {
			PlayerAlliance alliance = PlayerAllianceService.createAlliance(playersByRace.get(0), player, TeamType.AUTO_ALLIANCE);
			int allianceId = alliance.getObjectId();
			if (!instance.isRegistered(allianceId)) {
				instance.register(allianceId);
			}
		} else if (!playersByRace.isEmpty() && playersByRace.get(0).isInAlliance()) {
			PlayerAllianceService.addPlayer(playersByRace.get(0).getPlayerAlliance(), player);
		}
		int objectId = player.getObjectId();
		if (!instance.isRegistered(objectId)) {
			instance.register(objectId);
		}
		PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE((byte) 1, 27));
	}

	@Override
	public void onPressEnter(Player player) {
		super.onPressEnter(player);
		KamarBattlefieldService.getInstance().addCooldown(player);
		instance.getInstanceHandler().portToStartPosition(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		super.unregister(player);
		PlayerAllianceService.removePlayer(player);
	}
}
