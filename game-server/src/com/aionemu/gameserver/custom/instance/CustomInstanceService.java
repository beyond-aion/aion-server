package com.aionemu.gameserver.custom.instance;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModelEntry;
import com.aionemu.gameserver.dao.CustomInstanceDAO;
import com.aionemu.gameserver.dao.CustomInstancePlayerModelEntryDAO;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Jo, Estrayl
 */
public class CustomInstanceService {

	private static final Logger log = LoggerFactory.getLogger("CUSTOM_INSTANCE_LOG");
	private static final int CUSTOM_INSTANCE_WORLD_ID = 300070000; // roah chamber

	// Neural network related
	private Map<Integer, List<PlayerModelEntry>> playerModelEntriesCache = new ConcurrentHashMap<>();

	private CustomInstanceService() {
	}

	public boolean canEnter(int playerId) {
		return getPlayerRankObject(playerId).getLastEntry() < ServerTime.now().with(LocalTime.of(9, 0)).toEpochSecond() * 1000;
	}

	public void onEnter(Player player) {
		if (!updateLastEntry(player.getObjectId(), System.currentTimeMillis())) {
			PacketSendUtility.sendMessage(player, "Sorry. Some shugo broke our database, please report this in our bugtracker :(");
			return;
		}
		playerModelEntriesCache.put(player.getObjectId(), loadPlayerModelEntries(player.getObjectId()));
		WorldMapInstance wmi = InstanceService.getNextAvailableInstance(CUSTOM_INSTANCE_WORLD_ID, 0, (byte) 1, new RoahCustomInstanceHandler());
		InstanceService.registerPlayerWithInstance(wmi, player);
		TeleportService.teleportTo(player, wmi.getMapId(), wmi.getInstanceId(), 504.0f, 396.0f, 94.0f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
	}

	public CustomInstanceRank getPlayerRankObject(int playerId) {
		return DAOManager.getDAO(CustomInstanceDAO.class).loadPlayerRankObject(playerId);
	}

	public boolean updateLastEntry(int playerId, long newEntryTime) {
		CustomInstanceRank rankObj = getPlayerRankObject(playerId);
		rankObj.setLastEntry(newEntryTime);
		return DAOManager.getDAO(CustomInstanceDAO.class).storePlayer(rankObj);
	}

	public void changePlayerRank(int playerId, int newRank) {
		CustomInstanceRank rankObj = getPlayerRankObject(playerId);
		int oldRank = rankObj.getRank();
		rankObj.setRank(newRank);
		if (DAOManager.getDAO(CustomInstanceDAO.class).storePlayer(rankObj))
			log.info("[CI_ROAH] Changing instance rank for [playerId=" + playerId + "] from " + CustomInstanceRankEnum.getRankDescription(oldRank) + "("
				+ oldRank + ") to " + CustomInstanceRankEnum.getRankDescription(newRank) + "(" + newRank + ").");
	}

	public void recordPlayerModelEntry(Player player, Skill skill, VisibleObject target) {
		// FILTER: Only record roah custom instance skills for the moment
		if (player.getWorldId() != CUSTOM_INSTANCE_WORLD_ID)
			return;

		WorldMapInstance wmi = player.getPosition().getWorldMapInstance();
		if (!(wmi.getInstanceHandler() instanceof RoahCustomInstanceHandler) || !((RoahCustomInstanceHandler) wmi.getInstanceHandler()).isBossPhase()
			|| player.getPosition().getWorldMapInstance().getSoloPlayerObj() != player.getObjectId())
			return;

		List<PlayerModelEntry> entries = playerModelEntriesCache.get(player.getObjectId());
		if (entries == null) {
			entries = new ArrayList<>();
			playerModelEntriesCache.put(player.getObjectId(), entries);
		}

		entries.add(new PlayerModelEntry(player, skill.getSkillId(), target instanceof Creature ? (Creature) target : null));
	}

	private List<PlayerModelEntry> loadPlayerModelEntries(int playerId) {
		return DAOManager.getDAO(CustomInstancePlayerModelEntryDAO.class).loadPlayerModelEntries(playerId);
	}

	public void saveNewPlayerModelEntries(int playerId) {
		List<PlayerModelEntry> pmes = playerModelEntriesCache.remove(playerId);
		if (pmes == null)
			return;
		Collection<PlayerModelEntry> filteredEntries = pmes.stream().filter(Persistable.NEW).collect(Collectors.toList());
		DAOManager.getDAO(CustomInstancePlayerModelEntryDAO.class).insertNewRecords(filteredEntries);
	}

	public List<PlayerModelEntry> getPlayerModelEntries(int playerId) {
		List<PlayerModelEntry> entries = playerModelEntriesCache.get(playerId);
		if (entries == null) {
			entries = new ArrayList<>();
			playerModelEntriesCache.put(playerId, entries);
		}
		return entries;
	}

	private static class SingletonHolder {

		protected static final CustomInstanceService instance = new CustomInstanceService();
	}

	public static CustomInstanceService getInstance() {
		return SingletonHolder.instance;
	}
}
