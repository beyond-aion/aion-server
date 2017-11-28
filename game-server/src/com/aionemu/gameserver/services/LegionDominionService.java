package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.dao.LegionDominionDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.legionDominion.LegionDominionParticipantInfo;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.templates.LegionDominionLocationTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_DOMINION_LOC_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_DOMINION_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_INFO;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;

/**
 * @author Yeats
 *
 */
public class LegionDominionService {

	private static final LegionDominionService instance = new LegionDominionService();
	
	private Map<Integer, LegionDominionLocation> legionDominionLocations = new TreeMap<>();
	
	public static LegionDominionService getInstance() {
		return instance;
	}
	
	public void initLocations() {
		for (LegionDominionLocationTemplate temp : DataManager.LEGION_DOMINION_DATA.getLocationTemplates()) {
			legionDominionLocations.put(temp.getId(), new LegionDominionLocation(temp));
		}
		DAOManager.getDAO(LegionDominionDAO.class).loadLegionDominionLocations(legionDominionLocations);
		for (LegionDominionLocation loc : legionDominionLocations.values()) {
			loc.setParticipantInfo(DAOManager.getDAO(LegionDominionDAO.class).loadParticipants(loc));
		}
	}
	
	public Collection<LegionDominionLocation> getLegionDominions() {
		return legionDominionLocations.values();
	}
	
	public LegionDominionLocation getLegionDominionLoc(int locId) {
		return legionDominionLocations.get(locId);
	}

	/**
	 * @param legionId
	 * @param locId
	 * @return
	 */
	public boolean join(int legionId, int locId) {
		return legionDominionLocations.get(locId).join(legionId);
	}

	public LegionDominionLocation getLegionDominionByZone(String zoneName) {
		for (LegionDominionLocation loc : legionDominionLocations.values()) {
			if (loc.getZoneNameAsString().equalsIgnoreCase(zoneName)) {
				return loc;
			}
		}
		return null;
	}
	
	public void onFinishInstance(Legion legion, int points, long time) {
		if (legion != null) {
			LegionDominionLocation loc = getLegionDominionLoc(legion.getCurrentLegionDominion());
			if (loc != null) {
				LegionDominionParticipantInfo info = loc.getParticipantInfo(legion.getLegionId());
				if (info != null && info.getPoints() < points) {
					info.setDate(new Timestamp(System.currentTimeMillis()));
					info.setPoints(points);
					info.setTime((int) (time/1000));
					DAOManager.getDAO(LegionDominionDAO.class).updateInfo(info);
					loc.updateRanking();
				}
			}
		}
	}

	public void startWeeklyCalculation() {
		for (LegionDominionLocation loc : legionDominionLocations.values()) {
			//find winner and change locations legionId to winners Id
			LegionDominionParticipantInfo winner = loc.getWinner();
			if (winner != null) {
				Legion winningLegion = LegionService.getInstance().getLegion(winner.getLegionId());
				if (winningLegion != null && !winningLegion.isDisbanding()) {
					winningLegion.setOccupiedLegionDominion(loc.getLocationId());
					loc.setLegionId(winningLegion.getLegionId());
				} else {
					loc.setLegionId(0);
				}
			} else {
				loc.setLegionId(0);
			}
			loc.setOccupiedDate(new Timestamp(System.currentTimeMillis()));

			//reset all participated legions & store them to db
			for (LegionDominionParticipantInfo info : loc.getParticipantInfo().values()) {
				Legion legion = LegionService.getInstance().getLegion(info.getLegionId());
				if (legion != null) {
					if (winner != null && info.getLegionId() != winner.getLegionId()) {
						legion.setOccupiedLegionDominion(0);
					}
					legion.setLastLegionDominion(loc.getLocationId());
					legion.setCurrentLegionDominion(0);
					DAOManager.getDAO(LegionDAO.class).storeLegion(legion);
					DAOManager.getDAO(LegionDominionDAO.class).delete(info);
					PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_DOMINION_RANK(loc.getLocationId()));
					PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_INFO(legion));
				}
			}
			//reset locations participant info and update this location
			loc.reset();
			DAOManager.getDAO(LegionDominionDAO.class).updateLegionDominionLocation(loc);
		}

		World.getInstance().forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_LEGION_DOMINION_LOC_INFO()));
	}
}
