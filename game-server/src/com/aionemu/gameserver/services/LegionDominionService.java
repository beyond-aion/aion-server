package com.aionemu.gameserver.services;

import java.sql.Timestamp;
import java.time.DayOfWeek;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.LegionDAO;
import com.aionemu.gameserver.dao.LegionDominionDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.LetterType;
import com.aionemu.gameserver.model.legionDominion.LegionDominionLocation;
import com.aionemu.gameserver.model.legionDominion.LegionDominionParticipantInfo;
import com.aionemu.gameserver.model.team.legion.Legion;
import com.aionemu.gameserver.model.templates.LegionDominionInvasionRift;
import com.aionemu.gameserver.model.templates.LegionDominionLocationTemplate;
import com.aionemu.gameserver.model.templates.LegionDominionReward;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_DOMINION_LOC_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_DOMINION_RANK;
import com.aionemu.gameserver.network.aion.serverpackets.SM_LEGION_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.mail.SystemMailService;
import com.aionemu.gameserver.services.player.PlayerService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.time.ServerTime;

/**
 * @author Yeats
 */
public class LegionDominionService {

	private static final LegionDominionService instance = new LegionDominionService();

	private final Map<Integer, LegionDominionLocation> legionDominionLocations = new TreeMap<>();

	public static LegionDominionService getInstance() {
		return instance;
	}

	public void initLocations() {
		for (LegionDominionLocationTemplate temp : DataManager.LEGION_DOMINION_DATA.getLocationTemplates()) {
			legionDominionLocations.put(temp.getId(), new LegionDominionLocation(temp));
		}
		LegionDominionDAO.loadOrCreateLegionDominionLocations(legionDominionLocations);
		for (LegionDominionLocation loc : legionDominionLocations.values()) {
			loc.setParticipantInfo(LegionDominionDAO.loadParticipants(loc));
		}
	}

	public Collection<LegionDominionLocation> getLegionDominions() {
		return legionDominionLocations.values();
	}

	public LegionDominionLocation getLegionDominionLoc(int locId) {
		return legionDominionLocations.get(locId);
	}

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
					LegionDominionDAO.updateInfo(info);
					loc.updateRanking();
				}
			}
		}
	}

	public void startWeeklyCalculation() {
		for (LegionDominionLocation loc : legionDominionLocations.values()) {
			// determine winner
			List<LegionDominionParticipantInfo> legionRanking = loc.getLegionRanking(true);
			int newOccupyingLegionId = 0;

			// reset current occupying legion
			int previousOccupyingLegionId = loc.getLegionId();
			if (previousOccupyingLegionId != 0) {
				Legion legion = LegionService.getInstance().getLegion(previousOccupyingLegionId);
				updateLegionOccupation(legion, loc.getLocationId(), false);
			}

			// find winner of stonespear reach challenge
			LegionDominionParticipantInfo winner = legionRanking.isEmpty() ? null : legionRanking.get(0);
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
				if (info.getLegionId() != previousOccupyingLegionId || info.getLegionId() == newOccupyingLegionId) {
					Legion legion = LegionService.getInstance().getLegion(info.getLegionId());
					if (legion != null) {
						int occupiedId = 0;
						if (winner != null && legion.getLegionId() == newOccupyingLegionId)
							occupiedId = loc.getLocationId();
						updateLegionOccupation(legion, loc.getLocationId(), occupiedId > 0);
					}
				}
				LegionDominionDAO.delete(info);
			}

			Map<Integer, List<LegionDominionReward>> dominionRewards = loc.getRewards();
			for (int i = 0; i < legionRanking.size(); i++) {
				if (i >= dominionRewards.size())
					break;
				LegionDominionParticipantInfo participantInfo = legionRanking.get(i);
				Legion legion = LegionService.getInstance().getLegion(participantInfo.getLegionId());
				if (legion == null || legion.isDisbanding())
					continue;
				List<LegionDominionReward> legionRewards = dominionRewards.get(i+1);
				if (!legionRewards.isEmpty()) {
					String playerName = PlayerService.getPlayerName(legion.getBrigadeGeneral());
					for (LegionDominionReward reward : legionRewards) {
						// TODO send proper system (most likely $$GD_REWARD_MAIL) mail
						SystemMailService.sendMail("Legion Dominion", playerName, "Reward Mail", "", reward.getItemId(), reward.getCount(),
								0, LetterType.NORMAL);
					}
				}
			}
			// reset locations participant info and update this location
			loc.reset();
			LegionDominionDAO.updateLegionDominionLocation(loc);
		}
		PacketSendUtility.broadcastToWorld(new SM_LEGION_DOMINION_LOC_INFO());
	}

	private void updateLegionOccupation(Legion legion, int territorialId, boolean shouldOccupy) {
		if (legion == null)
			return;
		legion.setOccupiedLegionDominion(shouldOccupy ? territorialId : 0);
		legion.setLastLegionDominion(territorialId);
		legion.setCurrentLegionDominion(0);
		LegionDAO.storeLegion(legion);
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_DOMINION_RANK(territorialId));
		PacketSendUtility.broadcastToLegion(legion, new SM_LEGION_INFO(legion));
	}

	public boolean isInCalculationTime() {
		ZonedDateTime now = ServerTime.now();
		return now.getDayOfWeek() == DayOfWeek.WEDNESDAY && now.getHour() >= 8 && now.getHour() <= 10;
	}

	public boolean openInvasionRift(int territoryId) {
		LegionDominionLocation location = getLegionDominionLoc(territoryId);
		if (location == null || location.getInvasionRift() == null)
			return false;
		LegionDominionInvasionRift invasionRift = location.getInvasionRift();
		if (isInCalculationTime())
			return false;
		if (RiftService.getInstance().isRiftOpened(invasionRift.getRiftId()))
			return false;
		boolean openedSuccessfully = RiftService.getInstance().openRifts(invasionRift.getRiftId(), false);
		if (openedSuccessfully) {
			if (location.getRace() == Race.ELYOS) {
				PacketSendUtility.broadcastToWorld(SM_SYSTEM_MESSAGE.STR_MSG_LIGHT_SIDE_LEGION_DIRECT_PORTAL_OPEN());
			} else {
				PacketSendUtility.broadcastToWorld(SM_SYSTEM_MESSAGE.STR_MSG_DARK_SIDE_LEGION_DIRECT_PORTAL_OPEN());
			}
			// Schedule rift close
			ThreadPoolManager.getInstance().schedule(() -> RiftService.getInstance().closeRifts(invasionRift.getRiftId()),
				RiftService.getInstance().getDuration() * 3540 * 1000);
		}
		return openedSuccessfully;
	}

}
