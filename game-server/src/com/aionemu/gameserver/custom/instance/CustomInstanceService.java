package com.aionemu.gameserver.custom.instance;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.gameserver.custom.instance.neuralnetwork.PlayerModelEntry;
import com.aionemu.gameserver.dao.CustomInstanceDAO;
import com.aionemu.gameserver.dao.CustomInstancePlayerModelEntryDAO;
import com.aionemu.gameserver.instance.handlers.InstanceHandler;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.time.ServerTime;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Jo, Estrayl
 */
public class CustomInstanceService {

	private static final Logger log = LoggerFactory.getLogger("CUSTOM_INSTANCE_LOG");
	private static final int CUSTOM_INSTANCE_WORLD_ID = 300070000; // roah chamber

	// instance specific
	private Map<Integer, CustomInstanceRank> rankCache;

	// Neural network related
	private Map<Integer, List<PlayerModelEntry>> playerModelEntriesCache;

	private CustomInstanceService() {
		rankCache = DAOManager.getDAO(CustomInstanceDAO.class).loadPlayerRanks();
		playerModelEntriesCache = DAOManager.getDAO(CustomInstancePlayerModelEntryDAO.class).loadPlayerModelEntries();
	}

	public boolean canEnter(int playerId) {
		CustomInstanceRank rankObject = rankCache.get(playerId);
		if (rankObject == null) {
			rankObject = new CustomInstanceRank(playerId, 0, ServerTime.now().with(LocalTime.of(1, 0)).toEpochSecond(), PersistentState.NEW);
			rankCache.put(playerId, rankObject);
		}
		return rankObject.getLastEntry() < ServerTime.now().with(LocalTime.of(9, 0)).toEpochSecond() * 1000;
	}

	public void onEnter(Player player) {
		CustomInstanceRank rankObject = rankCache.get(player.getObjectId());
		rankObject.setLastEntry(System.currentTimeMillis());

		WorldMapInstance wmi = InstanceService.getNextAvailableInstance(CUSTOM_INSTANCE_WORLD_ID, 0, (byte) 1, new RoahCustomInstanceHandler());
		InstanceService.registerPlayerWithInstance(wmi, player);
		TeleportService.teleportTo(player, wmi.getMapId(), wmi.getInstanceId(), 504.0f, 396.0f, 94.0f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);

		log.info("[CI_ROAH] " + player + " entered custom instance with difficulty " + CustomInstanceRankEnum.getRankDescription(rankObject.getRank())
			+ "(" + rankObject.getRank() + ").");
	}

	public CustomInstanceRank getPlayerRankObject(int playerId) {
		return rankCache.get(playerId);
	}

	public void changePlayerRank(int playerId, int newRank) {
		CustomInstanceRank rankObject = rankCache.get(playerId);
		if (rankObject == null) {
			rankObject = new CustomInstanceRank(playerId, 0, ServerTime.now().with(LocalTime.of(1, 0)).toEpochSecond(), PersistentState.NEW);
			rankCache.put(playerId, rankObject);
		}
		rankObject.setRank(newRank);
	}

	public void recordPlayerModelEntry(Player player, Skill skill, VisibleObject target) {

		// FILTER: Only record roah custom instance skills for the moment
		if (player.getWorldId() != CUSTOM_INSTANCE_WORLD_ID)
			return;

		InstanceHandler ih = player.getPosition().getWorldMapInstance().getInstanceHandler();
		if (!(ih instanceof RoahCustomInstanceHandler) || !((RoahCustomInstanceHandler) ih).isBossPhase()
			|| ((RoahCustomInstanceHandler) ih).getPlayerId() != player.getObjectId())
			return;

		List<PlayerModelEntry> entries = playerModelEntriesCache.get(player.getObjectId());
		if (entries == null) {
			entries = new ArrayList<>();
			playerModelEntriesCache.put(player.getObjectId(), entries);
		}

		entries.add(new PlayerModelEntry(player, skill.getSkillId(), target instanceof Creature ? (Creature) target : null));
	}

	public void writePlayerModelEntriesToDB(int playerId) {
		List<PlayerModelEntry> pmes = playerModelEntriesCache.get(playerId);
		if (pmes != null) {
			Collection<PlayerModelEntry> filteredEntries = pmes.stream().filter(Persistable.NEW).collect(Collectors.toList());
			DAOManager.getDAO(CustomInstancePlayerModelEntryDAO.class).insertNewRecords(filteredEntries);
		}
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
