package com.aionemu.gameserver.model.team.alliance.events;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.events.AbstractTeamPlayerEvent;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ALLIANCE_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class AssignViceCaptainEvent extends AbstractTeamPlayerEvent<PlayerAlliance> {

	public static enum AssignType {

		PROMOTE,
		DEMOTE_CAPTAIN_TO_VICECAPTAIN,
		DEMOTE
	}

	private final AssignType assignType;

	public AssignViceCaptainEvent(PlayerAlliance team, Player eventPlayer, AssignType assignType) {
		super(team, eventPlayer);
		this.assignType = assignType;
	}

	@Override
	public boolean checkCondition() {
		return eventPlayer != null && eventPlayer.isOnline();
	}

	@Override
	public void handleEvent() {
		switch (assignType) {
			case DEMOTE:
				team.getViceCaptainIds().remove(eventPlayer.getObjectId());
				break;
			case PROMOTE:
				if (team.getViceCaptainIds().size() == 4) {
					PacketSendUtility.sendPacket(team.getLeaderObject(), SM_SYSTEM_MESSAGE.STR_FORCE_CANNOT_PROMOTE_MANAGER());
					return;
				}
				team.getViceCaptainIds().add(eventPlayer.getObjectId());
				break;
			case DEMOTE_CAPTAIN_TO_VICECAPTAIN:
				if (team.getViceCaptainIds().size() < 3) {
					team.getViceCaptainIds().add(eventPlayer.getObjectId());
				}
				break;
		}

		team.forEach(member -> {
			int messageId = 0;
			switch (assignType) {
				case PROMOTE:
					messageId = SM_ALLIANCE_INFO.VICECAPTAIN_PROMOTE;
					break;
				case DEMOTE:
					messageId = SM_ALLIANCE_INFO.VICECAPTAIN_DEMOTE;
					break;
			}
			// TODO check whether same is sent to eventPlayer
			PacketSendUtility.sendPacket(member, new SM_ALLIANCE_INFO(team, messageId, eventPlayer.getName()));
		});

		if (team.isInLeague()) {
			team.getLeague().broadcast();
		}
	}

}
