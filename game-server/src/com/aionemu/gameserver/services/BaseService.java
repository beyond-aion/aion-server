package com.aionemu.gameserver.services;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.base.BaseLocation;
import com.aionemu.gameserver.services.base.Base;

/**
 * @author Source
 */
public class BaseService {

	private static final Logger log = LoggerFactory.getLogger(BaseService.class);
	private final Map<Integer, Base<?>> active = new ConcurrentHashMap<Integer, Base<?>>();
	private Map<Integer, BaseLocation> bases;

	public void initBaseLocations() {
		log.info("Initializing bases...");
		bases = DataManager.BASE_DATA.getBaseLocations();
		log.info("Loaded " + bases.size() + " bases.");
	}

	public void initBases() {
		for (BaseLocation base : getBaseLocations().values()) {
			start(base.getId());
		}
	}

	public Map<Integer, BaseLocation> getBaseLocations() {
		return bases;
	}

	public BaseLocation getBaseLocation(int id) {
		return bases.get(id);
	}

	public void start(final int id) {
		final Base<?> base;

		synchronized (this) {
			if (active.containsKey(id)) {
				return;
			}
			base = new Base<BaseLocation>(getBaseLocation(id));
			active.put(id, base);
		}

		base.start();
	}

	public void stop(int id) {
		if (!isActive(id)) {
			log.info("Trying to stop not active base:" + id);
			return;
		}

		Base<?> base;
		synchronized (this) {
			base = active.remove(id);
		}

		if (base == null || base.isFinished()) {
			log.info("Trying to stop null or finished base:" + id);
			return;
		}

		base.stop();
		start(id);
	}

	public void capture(int id, Race race) {
		if (!isActive(id)) {
			log.info("Detecting not active base capture.");
			return;
		}

		getActiveBase(id).setRace(race);
		stop(id);
	}

	public boolean isActive(int id) {
		return active.containsKey(id);
	}

	public Base<?> getActiveBase(int id) {
		return active.get(id);
	}

	public static BaseService getInstance() {
		return BaseServiceHolder.INSTANCE;
	}

	private static class BaseServiceHolder {

		private static final BaseService INSTANCE = new BaseService();
	}

}
