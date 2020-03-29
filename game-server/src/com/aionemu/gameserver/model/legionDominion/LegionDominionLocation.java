package com.aionemu.gameserver.model.legionDominion;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.LegionDominionDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.templates.LegionDominionLocationTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_DOMINION_RANK;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Yeats
 */
public class LegionDominionLocation {

	private final LegionDominionLocationTemplate template;
	private final String zoneName;
	private int legionId;
	private Timestamp occupiedDate;
	private Map<Integer, LegionDominionParticipantInfo> participantInfo = new TreeMap<>();
	private List<Integer> legionsSortedByrank = new ArrayList<>();

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

	public List<Integer> getRank() {
		return legionsSortedByrank;
	}

	public int getRank(int legionId) {
		if (legionsSortedByrank.contains(legionId)) {
			return legionsSortedByrank.indexOf(legionId) + 1;
		}
		return 0;
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
			DAOManager.getDAO(LegionDominionDAO.class).storeNewInfo(getLocationId(), info);
		} else {
			DAOManager.getDAO(LegionDominionDAO.class).updateInfo(info);
		}
	}

	public List<LegionDominionParticipantInfo> getSortedTop25Participants(LegionDominionParticipantInfo curLegion) {
		List<LegionDominionParticipantInfo> info = new ArrayList<>();
		info.addAll(participantInfo.values());
		if (!info.isEmpty()) {
			info.sort(null);
			info = info.subList(0, (25 > info.size()) ? (info.size()) : 25); // = 25 entries
			if (info.contains(curLegion)) {
				return info;
			} else {
				info.remove(info.size() - 1); // remove last index
				info.add(info.size(), (curLegion)); // add cur Legion to last index
			}
		}
		return info;
	}

	public LegionDominionParticipantInfo getWinner() {
		List<LegionDominionParticipantInfo> info = new ArrayList<>();
		info.addAll(participantInfo.values());
		if (!info.isEmpty()) {
			info.sort(null);
			if (info.get(0) != null && info.get(0).getPoints() > 0) {
				return info.get(0);
			}
		}
		return null;
	}

	public LegionDominionParticipantInfo getParticipantInfo(int legionId) {
		if (!participantInfo.isEmpty() && participantInfo.containsKey(legionId)) {
			return participantInfo.get(legionId);
		}
		return null;
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
		legionsSortedByrank = new ArrayList<>();
	}
}
