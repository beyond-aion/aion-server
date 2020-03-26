package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

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

	public void onFinishInstance(Legion legion, int points, long time) {
		if (legion != null) {
			LegionDominionLocation loc = getLegionDominionLoc(legion.getCurrentLegionDominion());
			if (loc != null) {
				LegionDominionParticipantInfo info = loc.getParticipantInfo(legion.getLegionId());
				if (info != null && info.getPoints() < points) {
					info.setDate(new Timestamp(System.currentTimeMillis()));
					info.setPoints(points);
					info.setTime((int) (time / 1000));
					DAOManager.getDAO(LegionDominionDAO.class).updateInfo(info);
					loc.updateRanking();
				}
			}
		}
	}

	private void storeAndUpdateDominionStatusForLegion(Legion legion, int locationId) {
		DAOManager.getDAO(LegionDAO.class).storeLegion(legion);
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_DOMINION_RANK(locationId));
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_INFO(legion));
	}

	public void startWeeklyCalculation() {
		for (LegionDominionLocation loc : legionDominionLocations.values()) {
			// reset current occupying legion
			int previousOccupyingLegionId = loc.getLegionId();
			if (previousOccupyingLegionId != 0) {
				Legion legion = LegionService.getInstance().getLegion(previousOccupyingLegionId);
				if (legion != null) {
					legion.setOccupiedLegionDominion(0);
					legion.setLastLegionDominion(loc.getLocationId());
					legion.setCurrentLegionDominion(0);
					storeAndUpdateDominionStatusForLegion(legion, loc.getLocationId());
				}
			}

			// find winner of stonespear reach challenge
			int newOccupyingLegionId = 0;
			LegionDominionParticipantInfo winner = loc.getWinner();
			if (winner != null) {
				Legion winningLegion = LegionService.getInstance().getLegion(winner.getLegionId());
				if (winningLegion != null) {
					if (!winningLegion.isDisbanding()) {
						newOccupyingLegionId = winningLegion.getLegionId();
					} else {
						LoggerFactory.getLogger(LegionDominionService.class).warn(
							"[Legion dominion] Skipped occupy of location {} for legion [id={}, name={}] due to disbanding", loc.getLocationId(),
							winningLegion.getLegionId(), winningLegion.getName());
					}
				}
			}

			loc.setLegionId(newOccupyingLegionId);
			loc.setOccupiedDate(new Timestamp(System.currentTimeMillis()));

			// update all participated legions & store them to db
			for (LegionDominionParticipantInfo info : loc.getParticipantInfo().values()) {
				// skip previous legion since its already updated and didnt reoccupy it
				if (info.getLegionId() == previousOccupyingLegionId && info.getLegionId() != newOccupyingLegionId)
					continue;
				Legion legion = LegionService.getInstance().getLegion(info.getLegionId());
				if (legion != null) {
					if (winner != null && legion.getLegionId() == newOccupyingLegionId) {
						legion.setOccupiedLegionDominion(loc.getLocationId());
					} else {
						legion.setOccupiedLegionDominion(0);
					}
					legion.setLastLegionDominion(loc.getLocationId());
					legion.setCurrentLegionDominion(0);
					storeAndUpdateDominionStatusForLegion(legion, loc.getLocationId());
				}
				DAOManager.getDAO(LegionDominionDAO.class).delete(info);
			}
			// reset locations participant info and update this location
			loc.reset();
			DAOManager.getDAO(LegionDominionDAO.class).updateLegionDominionLocation(loc);
		}

		World.getInstance().forEachPlayer(player -> PacketSendUtility.sendPacket(player, new SM_LEGION_DOMINION_LOC_INFO()));
	}
}
