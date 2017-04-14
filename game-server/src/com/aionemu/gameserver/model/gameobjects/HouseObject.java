package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.PlaceableObjectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.IExpirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.templates.housing.HousingCategory;
import com.aionemu.gameserver.model.templates.housing.LimitType;
import com.aionemu.gameserver.model.templates.housing.PlaceArea;
import com.aionemu.gameserver.model.templates.housing.PlaceLocation;
import com.aionemu.gameserver.model.templates.housing.PlaceableHouseObject;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_EDIT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author Rolandas
 */
public abstract class HouseObject<T extends PlaceableHouseObject> extends VisibleObject implements IExpirable {

	private int expireEnd;
	private float x;
	private float y;
	private float z;
	private byte heading;
	private int ownerUsedCount = 0;
	private int visitorUsedCount = 0;
	private Integer color = null;
	private int colorExpireEnd;

	private House ownerHouse;
	// don't set it directly, ever!!! Use setPersistentState() method instead
	private PersistentState persistentState = PersistentState.NEW;

	public HouseObject(House owner, int objId, int templateId) {
		super(objId, new PlaceableObjectController<T>(), null, DataManager.HOUSING_OBJECT_DATA.getTemplateById(templateId), null);
		this.ownerHouse = owner;
		getController().setOwner(this);
		setKnownlist(new PlayerAwareKnownList(this));
	}

	public PersistentState getPersistentState() {
		return persistentState;
	}

	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else if (this.persistentState != PersistentState.DELETED) {
					this.persistentState = PersistentState.DELETED;
					ownerHouse.getRegistry().setPersistentState(PersistentState.UPDATE_REQUIRED);
				}
				break;
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW) {
					ownerHouse.getRegistry().setPersistentState(PersistentState.UPDATE_REQUIRED);
					break;
				}
			default:
				if (this.persistentState != persistentState) {
					this.persistentState = persistentState;
					ownerHouse.getRegistry().setPersistentState(PersistentState.UPDATE_REQUIRED);
				}
		}
	}

	@Override
	public int getExpireTime() {
		return expireEnd;
	}

	public void setExpireTime(int time) {
		expireEnd = time;
	}

	@Override
	public void expireEnd(Player player) {
		final int descId = getObjectTemplate().getNameId();
		final SM_SYSTEM_MESSAGE msg = SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_DELETE_EXPIRE_TIME(descId);
		if (isSpawnedByPlayer())
			selfDestroy(player, msg);
		else {
			PacketSendUtility.sendPacket(player, new SM_HOUSE_EDIT(4, 1, getObjectId()));
			PacketSendUtility.sendPacket(player, msg);
			ownerHouse.getRegistry().removeObject(getObjectId());
		}
	}

	protected void selfDestroy(final Player player, SM_SYSTEM_MESSAGE message) {
		PacketSendUtility.sendPacket(player, new SM_HOUSE_EDIT(7, 0, getObjectId()));
		getController().delete();
		PacketSendUtility.sendPacket(player, new SM_HOUSE_EDIT(4, 1, getObjectId()));
		PacketSendUtility.sendPacket(player, message);
		ownerHouse.getRegistry().removeObject(getObjectId());
		Player owner = World.getInstance().findPlayer(ownerHouse.getOwnerId());
		// if owner is not online, we should save his items
		if (owner == null || !owner.isOnline())
			ownerHouse.getRegistry().save();
	}

	/**
	 * Gets seconds left for the object use. If has no expiration return -1
	 * 
	 * @return
	 */
	public int getUseSecondsLeft() {
		if (expireEnd == 0)
			return -1;
		int diff = expireEnd - (int) (System.currentTimeMillis() / 1000);
		if (diff < 0)
			return 0;
		return diff;
	}

	@Override
	public void expireMessage(Player player, int time) {
		// TODO Add if it exists
	}

	@Override
	@SuppressWarnings("unchecked")
	public T getObjectTemplate() {
		return (T) super.getObjectTemplate();
	}

	@Override
	public float getX() {
		return x;
	}

	public void setX(float x) {
		if (this.x != x) {
			this.x = x;
			setPersistentState(PersistentState.UPDATE_REQUIRED);
			if (position != null)
				position.setXYZH(x, null, null, null);
		}
	}

	@Override
	public float getY() {
		return y;
	}

	public void setY(float y) {
		if (this.y != y) {
			this.y = y;
			setPersistentState(PersistentState.UPDATE_REQUIRED);
			if (position != null)
				position.setXYZH(null, y, null, null);
		}
	}

	@Override
	public float getZ() {
		return z;
	}

	public void setZ(float z) {
		if (this.z != z) {
			this.z = z;
			setPersistentState(PersistentState.UPDATE_REQUIRED);
			if (position != null)
				position.setXYZH(null, null, z, null);
		}
	}

	@Override
	public byte getHeading() {
		return heading;
	}

	public void setHeading(byte heading) {
		if (this.heading != heading) {
			this.heading = heading;
			setPersistentState(PersistentState.UPDATE_REQUIRED);
			if (position != null)
				position.setXYZH(null, null, null, heading);
		}
	}

	public int getRotation() {
		int rotation = this.heading & 0xFF;
		return rotation * 3;
	}

	public void setRotation(int rotation) {
		setHeading((byte) Math.ceil(rotation / 3f));
	}

	public PlaceLocation getPlaceLocation() {
		return getObjectTemplate().getLocation();
	}

	public PlaceArea getPlaceArea() {
		return getObjectTemplate().getArea();
	}

	public int getPlacementLimit(boolean trial) {
		LimitType limitType = getObjectTemplate().getPlacementLimit();
		HouseType size = HouseType.fromValue(ownerHouse.getBuilding().getSize());
		if (trial)
			return limitType.getTrialObjectPlaceLimit(size);
		return limitType.getObjectPlaceLimit(size);
	}

	public ItemQuality getQuality() {
		return getObjectTemplate().getQuality();
	}

	public float getTalkingDistance() {
		return getObjectTemplate().getTalkingDistance();
	}

	public HousingCategory getCategory() {
		return getObjectTemplate().getCategory();
	}

	public House getOwnerHouse() {
		return ownerHouse;
	}

	public int getPlayerId() {
		return ownerHouse.getOwnerId();
	}

	public int getOwnerUsedCount() {
		return ownerUsedCount;
	}

	public void incrementOwnerUsedCount() {
		this.ownerUsedCount++;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public void incrementVisitorUsedCount() {
		this.visitorUsedCount++;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
		Player owner = World.getInstance().findPlayer(ownerHouse.getOwnerId());
		// if owner is not online, we should save his items
		if (owner == null || !owner.isOnline())
			ownerHouse.getRegistry().save();
	}

	public void setOwnerUsedCount(int ownerUsedCount) {
		if (this.ownerUsedCount != ownerUsedCount) {
			this.ownerUsedCount = ownerUsedCount;
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
	}

	public int getVisitorUsedCount() {
		return visitorUsedCount;
	}

	public void setVisitorUsedCount(int visitorUsedCount) {
		if (this.visitorUsedCount != visitorUsedCount) {
			this.visitorUsedCount = visitorUsedCount;
			setPersistentState(PersistentState.UPDATE_REQUIRED);
		}
	}

	/**
	 * Means the player has it spawned, not the game server
	 */
	public boolean isSpawnedByPlayer() {
		return x != 0 || y != 0 || z != 0;
	}

	@SuppressWarnings("unchecked")
	@Override
	public PlaceableObjectController<T> getController() {
		return (PlaceableObjectController<T>) super.getController();
	}

	public void spawn() {
		if (!isSpawnedByPlayer())
			return;
		World w = World.getInstance();
		if (position == null || !isSpawned()) {
			position = w.createPosition(ownerHouse.getWorldId(), x, y, z, heading, ownerHouse.getInstanceId());
			SpawnEngine.bringIntoWorld(this);
		}
		updateKnownlist();
	}

	/**
	 * Removes house from spawn but it remains in registry
	 */
	public void removeFromHouse() {
		this.setX(0);
		this.setY(0);
		this.setZ(0);
		this.setHeading((byte) 0);
	}

	public void onUse(Player player) {
	}

	public void onDialogRequest(Player player) {
		onUse(player);
	}

	public void onDespawn() {

	}

	public Integer getColor() {
		return color;
	}

	public void setColor(Integer color) {
		this.color = color;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getColorExpireEnd() {
		return colorExpireEnd;
	}

	public void setColorExpireEnd(int colorExpireEnd) {
		this.colorExpireEnd = colorExpireEnd;
	}

}
