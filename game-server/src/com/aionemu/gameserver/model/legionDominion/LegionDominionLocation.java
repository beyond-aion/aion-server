package com.aionemu.gameserver.model.legionDominion;

import java.sql.Timestamp;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.LegionDominionDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.templates.LegionDominionLocationTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_DOMINION_RANK;
import com.aionemu.gameserver.services.LegionService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import javolution.util.FastTable;

/**
 * @author Yeats
 *
 */
public class LegionDominionLocation {
	
	private int id;
	private int worldId;
	private int nameId;
	private Race race;
	private String zoneName;
	private int legionId;
	private Timestamp occupiedDate;
	private Map<Integer, LegionDominionParticipantInfo> participantInfo = new TreeMap<>();
	private List<Integer> legionsSortedByrank = new FastTable<>();
	
	public LegionDominionLocation(LegionDominionLocationTemplate temp) {
		this.id = temp.getId();
		this.worldId = temp.getWorldId();
		this.race = temp.getRace();
		this.zoneName = temp.getZone() + "_" + temp.getWorldId();
		this.nameId = temp.getNameId();
	}
	
	public int getLegionId() {
		return legionId;
	}

	public Timestamp getOccupiedDate() {
		return occupiedDate;
	}

	public int getLocationId() {
		return id;
	}

	public int getWorldId() {
		return worldId;
	}

	public Race getRace() {
		return race;
	}

	public String getZoneNameAsString() {
		return zoneName;
	}
	
	public Map<Integer, LegionDominionParticipantInfo> getParticipantInfo() {
		return participantInfo;
	}
	
	public List<Integer> getRank() {
		return legionsSortedByrank;
	}
	
	public int getRank(int legionId) {
		if (legionsSortedByrank.contains(legionId)) {
			return legionsSortedByrank.indexOf(legionId) + 1 ;
		}
		return 0;
	}
	
	public int getNameId() {
		return nameId;
	}
	
	public void setLegionId(int legionId) {
		this.legionId = legionId;
	}

	public void setOccupiedDate(Timestamp timestamp) {
		occupiedDate = timestamp;
	}
	
	public void setParticipantInfo(Map<Integer, LegionDominionParticipantInfo> info) {
		participantInfo = info;
		//updateRank();
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
			DAOManager.getDAO(LegionDominionDAO.class).storeNewInfo(id, info);
		} else {
			DAOManager.getDAO(LegionDominionDAO.class).updateInfo(info);
		}
	}
	
	public List<LegionDominionParticipantInfo> getSortedTop25Participants(LegionDominionParticipantInfo curLegion) {
		List<LegionDominionParticipantInfo> info = new FastTable<>();
		info.addAll(participantInfo.values());
		if (!info.isEmpty()) {
			info.sort(null);
			info = info.subList(0, 25 > (info.size()) ? (info.size()) : 25); // = 25 entries
			if (info.contains(curLegion)) {
				return info;
			} else {
				info.remove(info.size()-1); //remove last index
				info.add(info.size(), (curLegion)); //add cur Legion to last index
			}
		}
		return info;
	}

	public LegionDominionParticipantInfo getWinner() {
		List<LegionDominionParticipantInfo> info = new FastTable<>();
		info.addAll(participantInfo.values());
		if (!info.isEmpty()) {
			info.sort(null);
			if (info.get(0) != null) {
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
			if (legion != null) {
				for (Player p : legion.getOnlineLegionMembers()) {
					PacketSendUtility.sendPacket(p, new SM_LEGION_DOMINION_RANK(id));
				}
			}
		}
	}

	public synchronized void reset() {
		participantInfo = new TreeMap<>();
		legionsSortedByrank = new FastTable<>();
	}
}
