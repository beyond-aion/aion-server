package com.aionemu.gameserver.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.model.base.SiegeBaseLocation;
import com.aionemu.gameserver.model.base.StainedBaseLocation;
import com.aionemu.gameserver.model.templates.base.BaseTemplate;
import com.aionemu.gameserver.services.base.Base;
import com.aionemu.gameserver.services.base.BaseException;
import com.aionemu.gameserver.services.base.CasualBase;
import com.aionemu.gameserver.services.base.SiegeBase;
import com.aionemu.gameserver.services.base.StainedBase;
import com.aionemu.gameserver.spawnengine.SpawnHandlerType;

/**
 * @author Source, Estrayl
 */
public class BaseService {

	private final static Logger log = LoggerFactory.getLogger(BaseService.class);
	private final static BaseService instance = new BaseService();
	private final Map<Integer, Base<?>> activeBases = new ConcurrentHashMap<>();
	private final Map<Integer, BaseLocation> allBases = new HashMap<>();
	private final Map<Integer, BaseLocation> casualBases = new HashMap<>();
	private final Map<Integer, SiegeBaseLocation> siegeBases = new HashMap<>();
	private final Map<Integer, StainedBaseLocation> stainedBases = new HashMap<>();

	/**
	 * Initializes all base locations
	 */
	private BaseService() {
		log.info("Initializing bases...");

		for (BaseTemplate template : DataManager.BASE_DATA.getAllBaseTemplates()) {
			switch (template.getType()) {
				case CASUAL:
					BaseLocation b = new BaseLocation(template);
					casualBases.put(template.getId(), b);
					allBases.put(template.getId(), b);
					break;
				case SIEGE:
					SiegeBaseLocation s = new SiegeBaseLocation(template);
					siegeBases.put(template.getId(), s);
					allBases.put(template.getId(), s);
					break;
				case STAINED:
					StainedBaseLocation st = new StainedBaseLocation(template);
					stainedBases.put(template.getId(), st);
					allBases.put(template.getId(), st);
					break;
				default:
					throw new UnsupportedOperationException("Unhandled base type " + template.getType() + ", couldn't initialize BaseService");
			}
		}
	}

	/**
	 * Executes start of all casual and stained bases.
	 */
	public final void initBases() {
		casualBases.values().forEach(loc -> start(loc.getId()));
		stainedBases.values().forEach(sLoc -> start(sLoc.getId()));
	}

	/**
	 * Generates a new BaseObject for given id
	 */
	public final void start(int id) {
		final Base<?> base;

		synchronized (this) {
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
	 * @return A type-specific base object for given id, if a base location is given for the specific id.
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
	 */
	public final void capture(int id, Race race) {
		if (!isActive(id))
			return;

		Base<?> base = getActiveBase(id);
		if (race != null)
			base.setLocRace(race);
		stop(id);
		start(id);
		if (base instanceof StainedBase)
			handleStainedFeatures((StainedBase) base);
	}

	private final void handleStainedFeatures(StainedBase target) {
		List<StainedBase> spec = new ArrayList<>();
		for (Base<?> possibleSBase : activeBases.values()) {
			if (possibleSBase instanceof StainedBase) {
				StainedBase sb = (StainedBase) possibleSBase;
				if (!sb.equals(target)) {
					if (sb.getColor().equals(target.getColor()))
						spec.add(sb);
				}
			}
		}
		if (target.isEnhanced()) { // handles de-activation of enhanced mode
			target.setEnhanced(false);
			for (StainedBase sBase : spec) {
				sBase.despawnByHandlerType(SpawnHandlerType.OUTRIDER_ENHANCED);
				sBase.despawnByHandlerType(SpawnHandlerType.GUARDIAN);
				sBase.setEnhanced(false);
			}
		} else { // handles activation of enhanced mode
			Race race = target.getRace();
			if (race == Race.NPC) // no enhanced spawns for Balaur | Lepharists etc.
				return;
			byte unequalBases = 0;
			for (StainedBase sBase : spec) {
				if (!sBase.getRace().equals(race))
					unequalBases++;
			}
			if (unequalBases == 0) {
				target.setEnhanced(true);
				for (StainedBase sBase : spec) {
					sBase.setEnhanced(true);
					sBase.scheduleEnhancedSpawns();
				}
			}
		}
	}

	/**
	 * Removes base with given id from activeBases
	 * and stops it.
	 * Should only directly called for SiegeBases
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
