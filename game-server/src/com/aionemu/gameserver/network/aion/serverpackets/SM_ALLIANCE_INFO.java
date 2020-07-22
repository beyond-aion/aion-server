package com.aionemu.gameserver.network.aion.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.alliance.PlayerAlliance;
import com.aionemu.gameserver.model.team.common.legacy.LootGroupRules;
import com.aionemu.gameserver.model.team.league.League;
import com.aionemu.gameserver.network.aion.AionConnection;
import com.aionemu.gameserver.network.aion.AionServerPacket;

/**
 * @author Sarynth, xTz
 */
public class SM_ALLIANCE_INFO extends AionServerPacket {

	private LootGroupRules lootRules, lootLeagueRules;
	private PlayerAlliance alliance;
	private int leaderid;
	private int groupid;
	private int type;
	private int subType;
	private final int messageId;
	private final String message;
	private int leagueId;
	private final List<AllianceInfo> leagueData = new ArrayList<>();
	public static final int VICECAPTAIN_PROMOTE = 1300984;
	public static final int VICECAPTAIN_DEMOTE = 1300985;
	public static final int LEAGUE_ALLIANCE_ENTERED = 1400560; // Your alliance has joined %0's Alliance League.
	public static final int LEAGUE_JOINED_ALLIANCE = 1400561; // %0's alliance has joined the Alliance League.
	public static final int LEAGUE_LEFT_ME = 1400571;
	public static final int LEAGUE_LEFT_HIM = 1400572;
	public static final int LEAGUE_EXPEL = 1400574;
	public static final int LEAGUE_EXPELLED = 1400576;
	public static final int LEAGUE_DISPERSED = 1400579;

	private class AllianceInfo {

		private int alliancePosition;
		private int allianceObjectId;
		private int memberCount;
		private String captainName = "";
		private int captainWorldId = 0;

		public int getAlliancePosition() {
			return alliancePosition;
		}

		public void setAlliancePosition(int alliancePosition) {
			this.alliancePosition = alliancePosition;
		}

		public int getAllianceObjectId() {
			return allianceObjectId;
		}

		public void setAllianceObjectId(int allianceObjectId) {
			this.allianceObjectId = allianceObjectId;
		}

		public void setMemberCount(int memberCount) {
			this.memberCount = memberCount;
		}

		public int getMemberCount() {
			return memberCount;
		}

		public String getCaptainName() {
			return captainName;
		}

		public void setCaptainName(String captainName) {
			this.captainName = captainName;
		}

		public int getCaptainWorldId() {
			return captainWorldId;
		}

		public void setCaptainWorldId(int captainWorldId) {
			this.captainWorldId = captainWorldId;
		}

	}

	public SM_ALLIANCE_INFO(PlayerAlliance alliance) {
		this(alliance, 0, "", null);
	}

	public SM_ALLIANCE_INFO(PlayerAlliance alliance, PlayerAlliance skipped) {
		this(alliance, 0, "", skipped);
	}

	public SM_ALLIANCE_INFO(PlayerAlliance alliance, int messageId, String message) {
		this(alliance, messageId, message, null);
	}

	public SM_ALLIANCE_INFO(PlayerAlliance alliance, int messageId, String message, PlayerAlliance skipped) {
		this.alliance = alliance;
		groupid = alliance.getObjectId();
		leaderid = alliance.getLeader().getObjectId();
		lootRules = alliance.getLootGroupRules();
		type = alliance.getTeamType().getType();
		subType = alliance.getTeamType().getSubType();
		this.messageId = messageId;
		this.message = message;
		League league = alliance.getLeague();
		if (league != null) {
			leagueId = league.getTeamId();
			lootLeagueRules = league.getLootGroupRules();
			for (Player captain : league.getCaptains()) {
				AllianceInfo info = new AllianceInfo();
				PlayerAlliance captainAlliance = captain.getPlayerAlliance();
				if (captainAlliance != null) {
					info.setAlliancePosition(league.getMember(captainAlliance.getObjectId()).getLeaguePosition());
					info.setAllianceObjectId(captainAlliance.getObjectId());
					info.setMemberCount(captainAlliance.size());
					if (!captainAlliance.equals(skipped)) {
						info.setCaptainName(captain.getName());
						info.setCaptainWorldId(captain.getWorldId());
					}
				}
				leagueData.add(info);
			}
		}
	}

	@Override
	protected void writeImpl(AionConnection con) {
		Player player = con.getActivePlayer();
		writeH(alliance.groupSize());
		writeD(groupid);
		writeD(leaderid);
		writeD(player == null || player.getPosition() == null ? 0 : player.getWorldId());// mapId
		Collection<Integer> ids = alliance.getViceCaptainIds();
		for (Integer id : ids) {
			writeD(id);
		}
		for (int i = 0; i < 4 - ids.size(); i++) {
			writeD(0);
		}
		writeD(lootRules.getLootRule().getId());
		writeD(lootRules.getMisc());
		writeD(lootRules.getCommonItemAbove());
		writeD(lootRules.getSuperiorItemAbove());
		writeD(lootRules.getHeroicItemAbove());
		writeD(lootRules.getFabledItemAbove());
		writeD(lootRules.getEternalItemAbove());
		writeD(lootRules.getMythicItemAbove());
		writeD(0x02);
		writeC(0x00);
		writeD(type);
		writeD(subType); // 3.5
		writeD(leagueId);
		for (int a = 0; a < 4; a++) {
			writeD(a); // group num
			writeD(1000 + a); // group id
		}
		writeD(messageId); // System message ID
		writeS(messageId != 0 ? message : ""); // System message
		if (!leagueData.isEmpty()) {
			writeH(leagueData.size());
			writeD(lootLeagueRules.getLootRule().getId());
			writeD(lootLeagueRules.getMisc());
			writeD(lootLeagueRules.getCommonItemAbove());
			writeD(lootLeagueRules.getSuperiorItemAbove());
			writeD(lootLeagueRules.getHeroicItemAbove());
			writeD(lootLeagueRules.getFabledItemAbove());
			writeD(lootLeagueRules.getEternalItemAbove());
			writeD(lootLeagueRules.getMythicItemAbove());
			writeD(0x02);
			for (AllianceInfo info : leagueData) {
				writeD(info.getAlliancePosition());
				writeD(info.getAllianceObjectId());
				writeD(info.getMemberCount());
				writeS(info.getCaptainName());
				writeD(info.getCaptainWorldId());
			}
		}
	}

}
