package com.aionemu.gameserver.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.templates.flyring.FlyRingTemplate;

/**
 * @author xavier
 */
public class FlyRingService {

	Logger log = LoggerFactory.getLogger(FlyRingService.class);

	private static class SingletonHolder {

		protected static final FlyRingService instance = new FlyRingService();
	}

	public static final FlyRingService getInstance() {
		return SingletonHolder.instance;
	}

	private FlyRingService() {
		for (FlyRingTemplate t : DataManager.FLY_RING_DATA.getFlyRingTemplates()) {
			FlyRing f = new FlyRing(t, 0);
			f.spawn();
			log.debug("Added {} at m={},x={},y={},z={}", f.getName(), f.getWorldId(), f.getX(), f.getY(), f.getZ());
		}
	}
}
