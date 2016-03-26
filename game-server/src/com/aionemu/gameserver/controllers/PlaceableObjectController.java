package com.aionemu.gameserver.controllers;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.animations.ObjectDeleteAnimation;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.housing.PlaceableHouseObject;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Rolandas
 */
public class PlaceableObjectController<T extends PlaceableHouseObject> extends VisibleObjectController<HouseObject<T>> {

	ConcurrentHashMap<Integer, ActionObserver> observed = new ConcurrentHashMap<>();

	@Override
	public void see(VisibleObject object) {
		Player p = (Player) object;
		ActionObserver observer = new ActionObserver(ObserverType.MOVE);
		p.getObserveController().addObserver(observer);
		observed.put(p.getObjectId(), observer);
	}

	@Override
	public void notSee(VisibleObject object, ObjectDeleteAnimation animation) {
		Player p = (Player) object;
		ActionObserver observer = observed.remove(p.getObjectId());
		p.getObserveController().removeObserver(observer);
	}

	@Override
	public void onDespawn() {
		getOwner().onDespawn();
	}

	public void onDialogRequest(Player player) {
		if (!MathUtil.isInRange(getOwner(), player, getOwner().getObjectTemplate().getTalkingDistance() + 2)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_TOO_FAR_TO_USE());
			return;
		}
		getOwner().onDialogRequest(player);
	}
}
