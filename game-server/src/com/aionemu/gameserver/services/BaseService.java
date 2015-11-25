package com.aionemu.gameserver.services;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import javolution.util.FastTable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.model.base.SiegeBaseLocation;
import com.aionemu.gameserver.model.base.StainedBaseLocation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.services.base.Base;
import com.aionemu.gameserver.services.base.BaseException;
import com.aionemu.gameserver.services.base.CasualBase;
import com.aionemu.gameserver.services.base.SiegeBase;
import com.aionemu.gameserver.services.base.StainedBase;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;
import com.aionemu.gameserver.world.World;

/**
 * @author Source
 * @reworked Estrayl
 */
public class BaseService {
	private final static Logger log = LoggerFactory.getLogger(BaseService.class);
	private final static BaseService instance = new BaseService();
	private final Map<Integer, Base<?>> activeBases = new ConcurrentHashMap<>();
	private Map<Integer, BaseLocation> allBases = new ConcurrentHashMap<>();
	private Map<Integer, BaseLocation> casualBases;
	private Map<Integer, SiegeBaseLocation> siegeBases;
	private Map<Integer, StainedBaseLocation> stainedBases;
	
	/**
	 * Initializes all base locations
	 */
	public final void initBaseLocations() {
		log.info("Initializing bases...");
		casualBases = DataManager.BASE_DATA.getCasualBaseLocations();
		siegeBases = DataManager.BASE_DATA.getSiegeBaseLocations();
		stainedBases = DataManager.BASE_DATA.getStainedBaseLocations();		
		allBases = DataManager.BASE_DATA.getAllBaseLocations();
	}
	
	/**
	 * Executes start of all casual and stained bases.
	 */
	public final void initBases() {
		for (BaseLocation g : casualBases.values()) {
			start(g.getId());
		}
		for (StainedBaseLocation st : stainedBases.values()) {
			start(st.getId());
		}
	}

	/**
	 * Generates a new BaseObject for given id
	 * 
	 * @param id
	 */
	public final void start(int id) {
		final Base<?> base;	
		
		synchronized(this) {
			if (activeBases.containsKey(id))
				return;
			base = newBase(id);
			activeBases.put(id, base);
		}
		try {
			base.start();
		} catch (BaseException | NullPointerException e) {
			log.error("Base could not be started! ID:" + id, e);
		}
	}
	
	/**
	 * returns a type-specific base object for given id
	 * if a base location is given for the specific id.
	 * 
	 * @param id
	 * @return
	 */
	private final Base<?> newBase(int id) {
		if (casualBases.containsKey(id))
			return new CasualBase(casualBases.get(id));
		else if (stainedBases.containsKey(id))
			return new StainedBase(stainedBases.get(id));
		else if (siegeBases.containsKey(id))
			return new SiegeBase(siegeBases.get(id));
		else
			throw new BaseException("Unknown base handler for base! ID:" + id);
	}

	/**
	 * Checks for enhancement of stained bases.
	 * Executes or removes special stained features, if necessary.
	 * Otherwise the captured base will be stopped and newly initialized.
	 * 
	 * @param id
	 * @param race
	 */
	public final void capture(int id, Race race) {
		if (!isActive(id))
			return;
		
		Base<?> base = getActiveBase(id);
		if (base instanceof StainedBase)
			handleStainedFeatures((StainedBase) base);
		if (race != null)
			base.setLocRace(race);
		stop(id);
		start(id);
	}
	
	private final void handleStainedFeatures(StainedBase target) {
		List<StainedBase> spec = new FastTable<>();
		for (Base<?> possibleSBase : activeBases.values()) {
			if (possibleSBase instanceof StainedBase) {
				StainedBase sb = (StainedBase) possibleSBase;
				if (!sb.equals(target)) {
					if (sb.getColor().equals(target.getColor())) 
						spec.add(sb);
				}
			}
		}
		boolean isSgEnhanced = target.isEnhanced();
		if (isSgEnhanced) { //handles de-activation of enhanced mode
			target.setEnhanced(false);
			for (StainedBase sBase : spec) {
				despawnByHandlerType(SpawnHandlerType.OUTRIDER_ENHANCED, sBase.getId());
				despawnByHandlerType(SpawnHandlerType.GUARDIAN, sBase.getId());
				sBase.setEnhanced(false);
			}
		} else { //handles activation of enhanced mode
			Race race = target.getRace();
			byte unequalBases = 0;
			for (StainedBase sBase : spec) {
				if (!sBase.getRace().equals(race))
					unequalBases++;
			}
			if (unequalBases == 0) {
				target.setEnhanced(true);
				for (StainedBase sBase : spec) {
					sBase.cancelOutriderSpawnTask();
					despawnByHandlerType(SpawnHandlerType.OUTRIDER, sBase.getId());
					despawnByHandlerType(SpawnHandlerType.SENTINEL, sBase.getId());
					sBase.spawnBySpawnHandler(SpawnHandlerType.OUTRIDER, race);
					sBase.spawnBySpawnHandler(SpawnHandlerType.OUTRIDER_ENHANCED, race);
					sBase.spawnBySpawnHandler(SpawnHandlerType.SENTINEL, race);
					sBase.spawnBySpawnHandler(SpawnHandlerType.GUARDIAN, race);
					sBase.setEnhanced(true);
				}
			}
		}
	}
	
	/**
	 * Removes base with given id from activeBases
	 * and stops it.
	 * Should only directly called for SiegeBases
	 * 
	 * @param id
	 */
	public final void stop(int id) {
		if (!isActive(id))
			return;
		Base<?> garrison;
		synchronized (this) {
			garrison = activeBases.remove(id);
		}
		if (garrison == null || garrison.isFinished())
			return;
		try {
			garrison.stop();
		} catch (BaseException e) {
			log.error("Base could not be stopped! ID:" + id, e);
		}
	}
	
	/**
	 * Gets npcs for base id and despawns all which have the
	 * parameterized SpawnHandlerType
	 * 
	 * @param SpawnHandlerType type
	 * @param id
	 */
	private static void despawnByHandlerType(SpawnHandlerType type, int id) {
		for (Npc npc : World.getInstance().getBaseSpawns(id)) {
			if (npc == null)
				continue;
			if (npc.getSpawn().getHandlerType().equals(type)) {
				if (!npc.getLifeStats().isAlreadyDead())
					npc.getController().onDelete();
			}
		}
	}
	
	public Map<Integer, BaseLocation> getBaseLocations() {
		return allBases;
	}
	
	public BaseLocation getBaseLocation(int id) {
		return allBases.get(id);
	}
	
	public BaseLocation getCasualBaseLocation(int id) {
		return casualBases.get(id);
	}
	
	public SiegeBaseLocation getSiegeBaseLocation(int id) {
		return siegeBases.get(id);
	}
	
	public StainedBaseLocation getStainedBaseLocation(int id) {
		return stainedBases.get(id);
	}
	
	public Base<?> getActiveBase(int id) {
		return activeBases.get(id);
	}
	
	public boolean isActive(int id) {
		return activeBases.containsKey(id);
	}
	
	public static BaseService getInstance() {
		return instance;
	}
}