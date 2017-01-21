package com.aionemu.gameserver.model.team.league.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.model.team.league.LeagueService;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class LeagueInviteEvent extends RequestResponseHandler<Player> {

	private final Player invited;

	public LeagueInviteEvent(Player requester, Player invited) {
		super(requester);
		this.invited = invited;
	}

	@Override
	public void acceptRequest(Player requester, Player responder) {
		if (LeagueService.canInvite(requester, invited)) {
			League league = requester.getPlayerAlliance().getLeague();

			if (league == null) {
				league = LeagueService.createLeague(requester);
			}
			if (!invited.isInLeague()) {
				LeagueService.addAlliance(league, invited.getPlayerAlliance());
			}
		}
	}

	@Override
	public void denyRequest(Player requester, Player responder) {
		PacketSendUtility.sendPacket(requester, SM_SYSTEM_MESSAGE.STR_PARTY_ALLIANCE_HE_REJECT_INVITATION(responder.getName()));
	}

}
