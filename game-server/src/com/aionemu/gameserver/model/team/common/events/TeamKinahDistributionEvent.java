package com.aionemu.gameserver.model.team.common.events;

import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.TeamMember;
import com.aionemu.gameserver.model.team.TemporaryPlayerTeam;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class TeamKinahDistributionEvent<T extends TemporaryPlayerTeam<? extends TeamMember<Player>>> extends AbstractTeamPlayerEvent<T> {

	private final long amount;

	public TeamKinahDistributionEvent(T team, Player distributor, long amount) {
		super(team, distributor);
		this.amount = amount;
	}

	@Override
	public boolean checkCondition() {
		return team.hasMember(eventPlayer.getObjectId());
	}

	@Override
	public void handleEvent() {
		if (eventPlayer.getInventory().getKinah() < amount) {
			PacketSendUtility.sendPacket(eventPlayer, SM_SYSTEM_MESSAGE.STR_NOT_ENOUGH_MONEY());
			return;
		}

		List<Player> onlineMembers = team.getOnlineMembers();
		if (onlineMembers.size() > 1 && amount >= onlineMembers.size()) {
			long rewardPerPlayer = amount / onlineMembers.size();
			if (eventPlayer.getInventory().tryDecreaseKinah(amount)) {
				for (Player member : onlineMembers) {
					member.getInventory().increaseKinah(rewardPerPlayer);
					if (member.equals(eventPlayer)) {
						PacketSendUtility.sendPacket(member, SM_SYSTEM_MESSAGE.STR_MSG_SPLIT_ME_TO_B(amount, onlineMembers.size(), rewardPerPlayer));
					} else {
						PacketSendUtility.sendPacket(member,
							SM_SYSTEM_MESSAGE.STR_MSG_SPLIT_B_TO_ME(eventPlayer.getName(), amount, onlineMembers.size(), rewardPerPlayer));
					}
				}
			}
		}
	}

}
