package com.aionemu.gameserver.controllers;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.controllers.observer.FlyRingObserver;
import com.aionemu.gameserver.model.flyring.FlyRing;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.world.knownlist.KnownList.DeleteType;

/**
 * @author xavier
 */
public class FlyRingController extends VisibleObjectController<FlyRing> {

	   ConcurrentHashMap<Integer, FlyRingObserver> observed = new ConcurrentHashMap<>();

	@Override
	public void see(VisibleObject object) {
		Player p = (Player) object;
		FlyRingObserver observer = new FlyRingObserver(getOwner(), p);
		p.getObserveController().addObserver(observer);
		observed.put(p.getObjectId(), observer);
	}

	@Override
	public void notSee(VisibleObject object, DeleteType deleteType) {
		Player p = (Player) object;
		FlyRingObserver observer = observed.remove(p.getObjectId());
		if (deleteType.equals(DeleteType.OUT_RANGE)) {
			observer.moved();
		}
		p.getObserveController().removeObserver(observer);
	}
}
