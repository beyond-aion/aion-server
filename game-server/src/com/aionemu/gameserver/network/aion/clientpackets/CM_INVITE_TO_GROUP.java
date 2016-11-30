package com.aionemu.gameserver.network.aion.clientpackets;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.configs.administration.AdminConfig;
import com.aionemu.gameserver.model.gameobjects.player.DeniedStatus;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team2.alliance.PlayerAllianceService;
import com.aionemu.gameserver.model.team2.group.PlayerGroupService;
import com.aionemu.gameserver.model.team2.league.LeagueService;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.ChatUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Lyahim, ATracer
 * @modified Simple, Neon
 */
public class CM_INVITE_TO_GROUP extends AionClientPacket {

	private static final Logger log = LoggerFactory.getLogger(CM_INVITE_TO_GROUP.class);
	private String playerName;
	private int inviteType;

	public CM_INVITE_TO_GROUP(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
		inviteType = readUC();
		playerName = readS();
	}

	@Override
	protected void runImpl() {
		final Player inviter = getConnection().getActivePlayer();
		if (inviter.getLifeStats().isAlreadyDead()) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_PARTY_CANT_INVITE_WHEN_DEAD());
			return;
		}

		final Player invited = World.getInstance().findPlayer(ChatUtil.getRealAdminName(playerName));
		if (invited == null) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_NO_SUCH_USER(playerName));
			return;
		}

		if (invited.getPlayerSettings().isInDeniedStatus(DeniedStatus.GROUP)) {
			sendPacket(SM_SYSTEM_MESSAGE.STR_MSG_REJECTED_INVITE_PARTY(invited.getName(AdminConfig.CUSTOMTAG_ENABLE)));
			return;
		}

		if (invited.getEnemyState() == 2 || inviter.getEnemyState() == 2) {
			PacketSendUtility.sendMessage(inviter, "You can't invite players in FFA mode");
			return;
		}

		switch (inviteType) {
			case 0:
				PlayerGroupService.inviteToGroup(inviter, invited);
				break;
			case 12: // 2.5
				PlayerAllianceService.inviteToAlliance(inviter, invited);
				break;
			case 28:
				LeagueService.inviteToLeague(inviter, invited);
				break;
			default:
				log.warn("Received unknown invite type from player " + inviter.getName() + ": " + inviteType);
				break;
		}
	}
}
