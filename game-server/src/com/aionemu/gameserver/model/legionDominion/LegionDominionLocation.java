package com.aionemu.gameserver.model.legionDominion;

import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import com.aionemu.gameserver.configs.main.LegionConfig;
import com.aionemu.gameserver.dao.LegionDominionDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.templates.LegionDominionInvasionRift;
import com.aionemu.gameserver.model.templates.LegionDominionLocationTemplate;
import com.aionemu.gameserver.model.templates.LegionDominionReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_DOMINION_RANK;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Yeats, Sykra
 */
public class LegionDominionLocation {

	private final LegionDominionLocationTemplate template;
	private final String zoneName;
	private int legionId;
	private Timestamp occupiedDate;
	private Map<Integer, LegionDominionParticipantInfo> participantInfo = new TreeMap<>();

	public LegionDominionLocation(LegionDominionLocationTemplate template) {
		this.template = template;
		this.zoneName = template.getZone() + "_" + template.getWorldId();
	}

	public int getLocationId() {
		return template.getId();
	}

	public int getWorldId() {
		return template.getWorldId();
	}

	public Race getRace() {
		return template.getRace();
	}

	public String getL10n() {
		return template.getL10n();
	}

	public String getZoneNameAsString() {
		return zoneName;
	}

	public int getLegionId() {
		return legionId;
	}

	public LegionDominionInvasionRift getInvasionRift() {
		return template.getInvasionRift();
	}

	public void setLegionId(int legionId) {
		this.legionId = legionId;
	}

	public Timestamp getOccupiedDate() {
		return occupiedDate;
	}

	public void setOccupiedDate(Timestamp timestamp) {
		occupiedDate = timestamp;
	}

	public Map<Integer, LegionDominionParticipantInfo> getParticipantInfo() {
		return participantInfo;
	}

	public void setParticipantInfo(Map<Integer, LegionDominionParticipantInfo> info) {
		participantInfo = info;
	}

	public List<LegionDominionParticipantInfo> getLegionRanking(boolean removeNonEligibleLegions) {
		if (participantInfo.isEmpty())
			return Collections.emptyList();
		Stream<LegionDominionParticipantInfo> participantStream = participantInfo.values().stream();
		if (removeNonEligibleLegions)
			participantStream = participantStream.filter(info -> info.getPoints() > LegionConfig.STONESPEAR_REACH_MIN_POINTS_FOR_TERRITORY);
		return participantStream
			.sorted(Comparator.comparingInt(LegionDominionParticipantInfo::getPoints).reversed().thenComparingLong(LegionDominionParticipantInfo::getDate))
			.collect(Collectors.toList());
	}

	public Map<Integer, List<LegionDominionReward>> getRewards() {
		return template.getRewards().stream().collect(Collectors.groupingBy(LegionDominionReward::getRank));
	}

	public boolean join(int legionId) {
		if (participantInfo.containsKey(legionId)) {
			return false;
		}
		LegionDominionParticipantInfo info = new LegionDominionParticipantInfo();
		info.setLegionId(legionId);
		participantInfo.put(legionId, info);
		store(info, true);
		return true;
	}

	private void store(LegionDominionParticipantInfo info, boolean isNew) {
		if (isNew) {
			LegionDominionDAO.storeNewInfo(getLocationId(), info);
		} else {
			LegionDominionDAO.updateInfo(info);
		}
	}

	public List<LegionDominionParticipantInfo> getSortedTop25Participants(LegionDominionParticipantInfo curLegion) {
		List<LegionDominionParticipantInfo> info = getLegionRanking(false);
		if (info.isEmpty())
			return Collections.emptyList();
		info = info.subList(0,  Math.min(25, info.size()));
		if (!info.contains(curLegion)) {
			info.remove(info.size() -1);
			info.add(info.size() , curLegion);
		}
		return info;
	}

	public LegionDominionParticipantInfo getParticipantInfo(int legionId) {
		return participantInfo.get(legionId);
	}

	public void updateRanking() {
		for (LegionDominionParticipantInfo info : participantInfo.values()) {
			Legion legion = LegionService.getInstance().getLegion(info.getLegionId());
			if (legion != null)
				PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_DOMINION_RANK(getLocationId()));
		}
	}

	public synchronized void reset() {
		participantInfo = new TreeMap<>();
	}
}
