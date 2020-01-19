package com.aionemu.gameserver.model.town;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Persistable;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.L10n;
import com.aionemu.gameserver.model.templates.spawns.Spawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnGroup;
import com.aionemu.gameserver.model.templates.spawns.SpawnSpotTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TOWNS_LIST;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author ViAl
 */
public class Town implements Persistable, L10n {

	private int id;
	private int level;
	private int points;
	private Timestamp levelUpDate;
	private Race race;
	private PersistentState persistentState;
	private List<Npc> spawnedNpcs;

	/**
	 * Used only from DAO.
	 * 
	 * @param id
	 * @param level
	 * @param points
	 */
	public Town(int id, int level, int points, Race race, Timestamp levelUpDate) {
		this.id = id;
		this.level = level;
		this.points = points;
		this.levelUpDate = levelUpDate;
		this.race = race;
		this.persistentState = PersistentState.UPDATED;
		this.spawnedNpcs = new ArrayList<>();
		spawnNewObjects();
		GeoService.getInstance().updateTown(this.race, this.id, this.level);
	}

	/**
	 * Used for initial import from house templates.
	 * 
	 * @param id
	 */
	public Town(int id, Race race) {
		this(id, 1, 0, race, new Timestamp(60000));
		this.persistentState = PersistentState.NEW;
	}

	public int getId() {
		return id;
	}

	@Override
	public int getL10nId() {
		int idOffset = id - (race == Race.ELYOS ? 1001 : 2001);
		return (race == Race.ELYOS ? 403330 : 403360) + idOffset;
	}

	public int getLevel() {
		return level;
	}

	public int getPoints() {
		return points;
	}

	public synchronized void increasePoints(int amount) {
		switch (this.level) {
			case 1:
				if (this.points + amount >= 1000)
					increaseLevel();
				break;
			case 2:
				if (this.points + amount >= 2000)
					increaseLevel();
				break;
			case 3:
				if (this.points + amount >= 3000)
					increaseLevel();
				break;
			case 4:
				if (this.points + amount >= 4000)
					increaseLevel();
				break;
		}
		this.points += amount;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	private void increaseLevel() {
		this.level++;
		this.levelUpDate.setTime(System.currentTimeMillis());
		broadcastUpdate();
		despawnOldObjects();
		spawnNewObjects();
		GeoService.getInstance().updateTown(this.race, this.id, this.level);
	}

	private void broadcastUpdate() {
		Map<Integer, Town> data = new HashMap<>(1);
		data.put(this.id, this);
		final SM_TOWNS_LIST packet = new SM_TOWNS_LIST(data);
		World.getInstance().forEachPlayer(new Consumer<Player>() {

			@Override
			public void accept(Player player) {
				if (player.getRace() == race)
					PacketSendUtility.sendPacket(player, packet);
			}
		});
	}

	private void spawnNewObjects() {
		List<Spawn> newSpawns = DataManager.TOWN_SPAWNS_DATA.getSpawns(id, level);
		int worldId = DataManager.TOWN_SPAWNS_DATA.getWorldIdForTown(id);
		for (Spawn spawn : newSpawns) {
			SpawnGroup spawnGroup = new SpawnGroup(worldId, spawn);
			for (SpawnSpotTemplate sst : spawn.getSpawnSpotTemplates()) {
				VisibleObject object = SpawnEngine.spawnObject(new SpawnTemplate(spawnGroup, sst), 1);
				if (object instanceof Npc) {
					((Npc) object).setTownId(this.id);
					spawnedNpcs.add((Npc) object);
				}
			}
		}
	}

	private void despawnOldObjects() {
		for (Npc npc : spawnedNpcs)
			npc.getController().delete();
		spawnedNpcs.clear();
	}

	public Race getRace() {
		return this.race;
	}

	public Timestamp getLevelUpDate() {
		return levelUpDate;
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	public void setPersistentState(PersistentState state) {
		if (this.persistentState != PersistentState.NEW || state != PersistentState.UPDATE_REQUIRED)
			this.persistentState = state;
	}

}
