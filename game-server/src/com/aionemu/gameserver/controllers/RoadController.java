package com.aionemu.gameserver.controllers;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.controllers.observer.RoadObserver;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.road.Road;

/**
 * @author SheppeR
 */
public class RoadController extends VisibleObjectController<Road> {

	ConcurrentHashMap<Integer, RoadObserver> observed = new ConcurrentHashMap<>();

	@Override
	public void see(VisibleObject object) {
		Player p = (Player) object;
		RoadObserver observer = new RoadObserver(getOwner(), p);
		p.getObserveController().addObserver(observer);
		observed.put(p.getObjectId(), observer);
	}

	@Override
	public void notSee(VisibleObject object, boolean inRange) {
		Player p = (Player) object;
		RoadObserver observer = observed.remove(p.getObjectId());
		if (!inRange)
			observer.moved();

		p.getObserveController().removeObserver(observer);
	}
}
