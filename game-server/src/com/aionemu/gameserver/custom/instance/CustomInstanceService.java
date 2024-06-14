package com.aionemu.gameserver.custom.instance;

import java.time.LocalTime;
import java.time.ZonedDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.aionemu.gameserver.cache.HTMLCache;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModelEntry;
import com.aionemu.gameserver.dao.CustomInstanceDAO;
import com.aionemu.gameserver.dao.CustomInstancePlayerModelEntryDAO;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.HTMLService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.idfactory.IDFactory;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Jo, Estrayl
 */
public class CustomInstanceService {

	private final static List<Integer> restrictedSkills = Arrays.asList(0, 243, 244, 277, 282, 302, 912, 1178, 1327, 1346, 1347, 1757, 2106, 2167, 2400,
		2425, 2565, 2778, 3331, 3643, 3663, 3683, 3705, 3729, 3788, 3789, 3833, 3835, 3837, 3839, 3904, 3991, 4407, 8291, 10164, 11011, 13010, 13234,
		13231);

	public static final int REWARD_COIN_ID = 186000409;
	private static final int CUSTOM_INSTANCE_WORLD_ID = 300070000; // roah chamber
	private static final int LEADERBOARD_WINDOW_OBJECT_ID = IDFactory.getInstance().nextId();
	private static final int RESET_HOUR = 9;

	// Neural network related
	private final Map<Integer, List<PlayerModelEntry>> playerModelEntriesCache = new ConcurrentHashMap<>();

	private CustomInstanceService() {
	}

	public boolean canEnter(int playerId) {
		CustomInstanceRank playerRankObject = CustomInstanceDAO.loadPlayerRankObject(playerId);
		if (playerRankObject == null)
			return true;
		ZonedDateTime now = ServerTime.now();
		ZonedDateTime reUseTime = now.with(LocalTime.of(RESET_HOUR, 0));
		if (now.isBefore(reUseTime))
			reUseTime = reUseTime.minusDays(1);
		return playerRankObject.getLastEntry() < reUseTime.toEpochSecond() * 1000;
	}

	public void onEnter(Player player) {
		if (!updateLastEntry(player.getObjectId(), System.currentTimeMillis())) {
			PacketSendUtility.sendMessage(player, "Sorry. Some shugo broke our database, please report this in our bugtracker :(");
			return;
		}
		playerModelEntriesCache.put(player.getObjectId(), loadPlayerModelEntries(player.getObjectId()));
		WorldMapInstance wmi = InstanceService.getNextAvailableInstance(CUSTOM_INSTANCE_WORLD_ID, 0, (byte) 1, RoahCustomInstanceHandler::new, 1, true);
		wmi.register(player.getObjectId());
		TeleportService.teleportTo(player, wmi.getMapId(), wmi.getInstanceId(), 504.0f, 396.0f, 94.0f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
	}

	public CustomInstanceRank loadOrCreateRank(int playerId) {
		CustomInstanceRank customInstanceRank = CustomInstanceDAO.loadPlayerRankObject(playerId);
		if (customInstanceRank == null)
			customInstanceRank = new CustomInstanceRank(playerId, 0, System.currentTimeMillis(), 0, 0);
		return customInstanceRank;
	}

	public boolean resetEntryCooldown(int playerId) {
		CustomInstanceRank rankObj = CustomInstanceDAO.loadPlayerRankObject(playerId);
		if (rankObj == null)
			return false;
		ZonedDateTime now = ServerTime.now();
		ZonedDateTime reUseTime = now.with(LocalTime.of(RESET_HOUR, 0));
		if (now.isBefore(reUseTime))
			reUseTime = reUseTime.minusDays(1);
		if (rankObj.getLastEntry() < reUseTime.toEpochSecond() * 1000) {
			return false;
		} else {
			reUseTime = reUseTime.minusSeconds(1);
			rankObj.setLastEntry(reUseTime.toEpochSecond() * 1000);
			return CustomInstanceDAO.storePlayer(rankObj);
		}
	}

	public boolean updateLastEntry(int playerId, long newEntryTime) {
		CustomInstanceRank rankObj = loadOrCreateRank(playerId);
		rankObj.setLastEntry(newEntryTime);
		return CustomInstanceDAO.storePlayer(rankObj);
	}

	public boolean changePlayerRank(int playerId, int newRank, int achievedDps) {
		CustomInstanceRank rankObj = loadOrCreateRank(playerId);
		changeRank(rankObj, newRank);
		rankObj.setDps(achievedDps);
		return storeNewRankData(rankObj);
	}

	private boolean storeNewRankData(CustomInstanceRank rankObj) {
		return CustomInstanceDAO.storePlayer(rankObj);
	}

	private void changeRank(CustomInstanceRank rankObj, int newRank) {
		rankObj.setRank(newRank);
		if (newRank > rankObj.getMaxRank())
			rankObj.setMaxRank(newRank);
	}

	public void recordPlayerModelEntry(Player player, Skill skill, VisibleObject target) {
		// FILTER: Only record roah custom instance skills for the moment
		if (restrictedSkills.contains(skill.getSkillId()))
			return;

		List<PlayerModelEntry> entries = getPlayerModelEntries(player.getObjectId());
		entries.add(new PlayerModelEntry(player, skill.getSkillId(), target instanceof Creature creature ? creature : null));
	}

	private List<PlayerModelEntry> loadPlayerModelEntries(int playerId) {
		return CustomInstancePlayerModelEntryDAO.loadPlayerModelEntries(playerId);
	}

	public void saveNewPlayerModelEntries(int playerId) {
		List<PlayerModelEntry> pmes = playerModelEntriesCache.remove(playerId);
		if (pmes == null)
			return;
		Collection<PlayerModelEntry> filteredEntries = pmes.stream().filter(Persistable.NEW).collect(Collectors.toList());
		CustomInstancePlayerModelEntryDAO.insertNewRecords(filteredEntries);
	}

	public List<PlayerModelEntry> getPlayerModelEntries(int playerId) {
		return playerModelEntriesCache.computeIfAbsent(playerId, k -> new ArrayList<>());
	}

	public void openLeaderboard(Player player, Race race) {
		List<CustomInstanceRankedPlayer> rankedPlayers = CustomInstanceDAO.loadTop10(race);
		StringBuilder content = new StringBuilder("""
						<br><br><br>
						<font color='3E2601' size='4'>Eternal Challenge Leaderboard</font><br>
						<br>
						<img src='textures/ui/basic_sep1.dds' width='300' height='2'><br>
						<br><br>
						<table>
							<tr>
								<th align='right'><font color='3E2601'>#</font></th>
								<th>&nbsp;&nbsp;</th>
								<th colspan='2' align='center'><font color='3E2601'>Name</font></th>
								<th>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;</th>
								<th align='center'><font color='3E2601'>Rank</font></th>
							</tr>
						""");
		int rank = 1;
		for (CustomInstanceRankedPlayer p : rankedPlayers) {
			content.append("<tr>");
			content.append("  <td align='right'><font color='3E2601'>").append(rank++).append("</font></td>");
			content.append("  <td></td>");
			content.append("  <td background='textures/black_smoke2.DDS'><img src='").append(p.getPlayerClass().getIconImage()).append("' width='24'></td>");
			content.append("  <td><font color='3E2601'>").append(p.getName()).append("</font></td>");
			content.append("  <td></td>");
			content.append("  <td><font color='3E2601'>").append(CustomInstanceRankEnum.getRankDescription(p.getRank())).append("</font></td>");
			content.append("</tr>");
		}
		content.append("</table>");
		String page = HTMLCache.getInstance().getHTML("simplePageTemplate.xhtml");
		HTMLService.sendData(player, LEADERBOARD_WINDOW_OBJECT_ID, page.replace("%content%", content.toString()));
	}

	private static class SingletonHolder {

		protected static final CustomInstanceService instance = new CustomInstanceService();
	}

	public static CustomInstanceService getInstance() {
		return SingletonHolder.instance;
	}
}
