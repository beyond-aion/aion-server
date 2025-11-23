package com.aionemu.gameserver.services;

import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.base.*;
import com.aionemu.gameserver.model.templates.base.BaseTemplate;
import com.aionemu.gameserver.services.panesterra.PanesterraService;

/**
 * @author Source, Estrayl
 */
public class BaseService {

	private final static Logger log = LoggerFactory.getLogger(BaseService.class);
	private final static BaseService INSTANCE = new BaseService();
	private final Map<Integer, Base<?>> activeBases = new ConcurrentHashMap<>();
	private final Map<Integer, BaseLocation> allBaseLocations = new HashMap<>();

	/**
	 * Initializes all base locations
	 */
	private BaseService() {
		log.info("Initializing bases...");

		for (BaseTemplate template : DataManager.BASE_DATA.getAllBaseTemplates()) {
			BaseLocation loc = switch (template.getType()) {
				case CASUAL -> new BaseLocation(template);
				case SIEGE -> new SiegeBaseLocation(template);
				case STAINED -> new StainedBaseLocation(template);
				case PANESTERRA, PANESTERRA_ARTIFACT, PANESTERRA_FACTION_CAMP -> new PanesterraBaseLocation(template);
			};
			allBaseLocations.put(template.getId(), loc);
		}
	}

	/**
	 * Executes start of all casual and stained bases.
	 */
	public void initBases() {
		allBaseLocations.values().forEach(loc -> {
			switch (loc.getType()) {
				case CASUAL, STAINED, PANESTERRA, PANESTERRA_FACTION_CAMP -> start(loc.getId());
			}
		});
	}

	/**
	 * Generates a new BaseObject for given id
	 */
	public void start(int id) {
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
			log.error("Base could not be started! ID:{}", id, e);
		}
	}

	/**
	 * Removes base with given id from activeBases
	 * and stops it.
	 * Should only directly call for SiegeBases
	 */
	public void stop(int id) {
		if (!isActive(id))
			return;

		Base<?> base;
		synchronized (this) {
			base = activeBases.remove(id);
		}
		if (base == null || base.isStopped())
			return;

		try {
			base.stop();
		} catch (BaseException e) {
			log.error("Base could not be stopped! ID:{}", id, e);
		}
	}

	/**
	 * @return A type-specific base object for given id, if a base location is given for the specific id.
	 */
	private Base<?> newBase(int id) {
		BaseLocation loc = allBaseLocations.get(id);
		return switch (loc.getType()) {
			case CASUAL -> new CasualBase(loc);
			case STAINED -> new StainedBase((StainedBaseLocation) loc);
			case SIEGE -> new SiegeBase((SiegeBaseLocation) loc);
			case PANESTERRA -> new PanesterraBase((PanesterraBaseLocation) loc);
			case PANESTERRA_ARTIFACT -> new PanesterraArtifact((PanesterraBaseLocation) loc);
			case PANESTERRA_FACTION_CAMP -> new PanesterraFactionCamp((PanesterraBaseLocation) loc);
		};
	}

	public void capture(int id, BaseOccupier newOccupier) {
		if (!isActive(id))
			return;

		stop(id);

		BaseLocation loc = allBaseLocations.get(id);

		if (loc.getType() == BaseType.PANESTERRA_FACTION_CAMP)
			PanesterraService.getInstance().handleTeamElimination(loc.getOccupier().getPanesterraFaction());

		if (newOccupier != null)
			loc.setOccupier(newOccupier);

		start(id);

		if (loc instanceof StainedBaseLocation coloredLoc)
			handleStainedFeatures(coloredLoc.getColor(), newOccupier);
	}

	private void handleStainedFeatures(BaseColorType colorType, BaseOccupier newOccupier) {
		List<StainedBase> spec = activeBases.values().stream().filter(base -> base instanceof StainedBase sb && sb.getColor() == colorType)
			.map(base -> (StainedBase) base).toList();

		long equalBases = spec.stream().filter(base -> base.getOccupier() == newOccupier).count();
		if (newOccupier == BaseOccupier.BALAUR || equalBases < 3) {
			spec.forEach(StainedBase::deactivateEnhancedSpawns);
		} else {
			spec.forEach(StainedBase::scheduleEnhancedSpawns);
		}
	}

	public Collection<BaseLocation> getBaseLocations() {
		return allBaseLocations.values();
	}

	public Base<?> getActiveBase(int id) {
		return activeBases.get(id);
	}

	public BaseLocation getBaseLocation(int id) {
		return allBaseLocations.get(id);
	}

	public boolean isActive(int id) {
		return activeBases.containsKey(id);
	}

	public static BaseService getInstance() {
		return INSTANCE;
	}
}
