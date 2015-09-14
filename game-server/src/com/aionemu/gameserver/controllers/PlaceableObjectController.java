package com.aionemu.gameserver.controllers;

import java.util.concurrent.ConcurrentHashMap;

import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.housing.PlaceableHouseObject;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_HOUSE_OBJECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_OBJECT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.KnownList.DeleteType;

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
		PacketSendUtility.sendPacket(p, new SM_HOUSE_OBJECT(getOwner()));
	}

	@Override
	public void notSee(VisibleObject object, DeleteType deleteType) {
		Player p = (Player) object;
		ActionObserver observer = observed.remove(p.getObjectId());
		if (deleteType.equals(DeleteType.OUT_RANGE)) {
			observer.moved();
			PacketSendUtility.sendPacket(p, new SM_DELETE_HOUSE_OBJECT(getOwner().getObjectId()));
		}
		p.getObserveController().removeObserver(observer);
	}

	@Override
	public void onDespawn() {
		getOwner().onDespawn();
	}

	@Override
	public void delete() {
		if (getOwner().isSpawned())
			World.getInstance().despawn(getOwner(), false);
		World.getInstance().removeObject(getOwner());
	}

	public void onDialogRequest(Player player) {
		if (!MathUtil.isInRange(getOwner(), player, getOwner().getObjectTemplate().getTalkingDistance() + 2)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_TOO_FAR_TO_USE);
			return;
		}
		getOwner().onDialogRequest(player);
	}
}
