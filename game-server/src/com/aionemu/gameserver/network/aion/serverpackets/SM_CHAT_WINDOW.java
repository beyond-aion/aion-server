package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.Collection;
import java.util.Iterator;

import com.aionemu.gameserver.model.PlayerClass;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.group.PlayerGroup;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author ginho1, Cheatkiller, Neon
 */
public class SM_CHAT_WINDOW extends AionServerPacket {

	private Player target;
	private boolean isGroup;

	public SM_CHAT_WINDOW(Player target, boolean isGroup) {
		this.target = target;
		this.isGroup = isGroup;
	}

	@Override
	protected void writeImpl(AionConnection con) {
		if (target == null)
			return;

		if (isGroup) {
			if (target.isInGroup()) {
				writeC(2); // group
				writeS(target.getName(true));
				PlayerGroup group = target.getPlayerGroup();
				writeD(group.getTeamId());
				writeS(group.getLeader().getName());

				Collection<Player> members = group.getMembers();
				for (Player groupMember : members)
					writeC(groupMember.getLevel());

				for (int i = group.size(); i < 6; i++)
					writeC(0);

				for (Player groupMember : members)
					writeC(groupMember.getPlayerClass().getClassId());

				for (int i = group.size(); i < 6; i++)
					writeC(0);
			} else if (target.isInAlliance()) {
				writeC(3); // alliance

				PlayerAlliance alliance = target.getPlayerAlliance();

				writeS(alliance.getLeader().getName());
				writeD(alliance.getTeamId());

				Collection<Player> members = alliance.getMembers();
				Iterator<Player> membersIt = alliance.getMembers().iterator();
				String[] capitans = new String[] { "", "", "", "" };
				for (int i = 0; i < capitans.length; i++) {
					while (membersIt.hasNext()) {
						Player groupMember = membersIt.next();
						if (alliance.isSomeCaptain(groupMember)) {
							capitans[i] = groupMember.getName();
							break;
						}
					}
				}
				for (String capitan : capitans) {
					writeS(capitan);
				}
				writeH(0);
				writeC(alliance.size());
				writeH(alliance.getMinExpPlayerLevel());// LVL
				writeH(alliance.getMaxExpPlayerLevel());
				short[] counts = new short[PlayerClass.values().length];
				for (Player groupMember : members) {
					counts[groupMember.getPlayerClass().getClassId()]++;
				}
				for (short count : counts) {
					writeH(count);
				}
			} else {
				writeC(4); // no group
				writeS(target.getName(true));
				writeD(0); // no group yet
				writeC(target.getPlayerClass().getClassId());
				writeC(target.getLevel());
				writeC(0); // unk
			}
		} else {
			writeC(1);
			writeS(target.getName(true));
			writeS(target.getLegion() != null ? target.getLegion().getName() : "");
			writeC(target.getLevel());
			writeH(target.getPlayerClass().getClassId());
			writeS(target.getCommonData().getNote());
			writeD(1); // unk
			writeC(target.getAccount().getMembership()); // vip level
		}
	}
}
