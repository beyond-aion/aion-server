package com.aionemu.gameserver.model.gameobjects;

import com.aionemu.gameserver.controllers.PlaceableObjectController;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Expirable;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HouseRegistry;
import com.aionemu.gameserver.model.templates.housing.*;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_EDIT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.knownlist.PlayerAwareKnownList;

/**
 * @author Rolandas
 */
public abstract class HouseObject<T extends PlaceableHouseObject> extends VisibleObject implements Expirable, Persistable {

	private int expireEnd;
	private float x;
	private float y;
	private float z;
	private byte heading;
	private int ownerUsedCount = 0;
	private int visitorUsedCount = 0;
	private Integer color = null;
	private int colorExpireEnd;

	private final HouseRegistry registry;
	// don't set it directly, ever!!! Use setPersistentState() method instead
	private PersistentState persistentState = PersistentState.NEW;

	public HouseObject(HouseRegistry registry, int objId, int templateId) {
		this(registry, objId, templateId, false);
	}

	public HouseObject(HouseRegistry registry, int objId, int templateId, boolean autoReleaseObjectId) {
		super(objId, new PlaceableObjectController<T>(), null, DataManager.HOUSING_OBJECT_DATA.getTemplateById(templateId), null, autoReleaseObjectId);
		this.registry = registry;
		getController().setOwner(this);
		setKnownlist(new PlayerAwareKnownList(this));
	}

	@Override
	public PersistentState getPersistentState() {
		return persistentState;
	}

	@Override
	@SuppressWarnings("fallthrough")
	public void setPersistentState(PersistentState persistentState) {
		switch (persistentState) {
			case DELETED:
				if (this.persistentState == PersistentState.NEW)
					this.persistentState = PersistentState.NOACTION;
				else if (this.persistentState != PersistentState.DELETED) {
					this.persistentState = PersistentState.DELETED;
					registry.setPersistentState(PersistentState.UPDATE_REQUIRED);
				}
				break;
			case UPDATE_REQUIRED:
				if (this.persistentState == PersistentState.NEW) {
					registry.setPersistentState(PersistentState.UPDATE_REQUIRED);
					break;
				}
			default:
				if (this.persistentState != persistentState) {
					this.persistentState = persistentState;
					registry.setPersistentState(PersistentState.UPDATE_REQUIRED);
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
	public void onExpire(Player player) {
		despawnAndRemoveHouseObject(player, true);
	}

	protected void despawnAndRemoveHouseObject(Player player, boolean isExpired) {
		if (isSpawnedByPlayer()) {
			PacketSendUtility.sendPacket(player, new SM_HOUSE_EDIT(7, 0, getObjectId()));
			getController().delete();
		}
		PacketSendUtility.sendPacket(player, new SM_HOUSE_EDIT(4, 1, getObjectId()));
		if (isExpired)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_DELETE_EXPIRE_TIME(getObjectTemplate().getL10n()));
		else
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_OBJECT_DELETE_USE_COUNT_FINAL(getObjectTemplate().getL10n()));
		registry.discardObject(this, false);

		// if owner is not online, we should save his items
		Player owner = World.getInstance().getPlayer(registry.getOwner().getOwnerId());
		if (owner == null || !owner.isOnline())
			registry.save();
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
		setHeading(PositionUtil.convertAngleToHeading(rotation));
	}

	public PlaceLocation getPlaceLocation() {
		return getObjectTemplate().getLocation();
	}

	public PlaceArea getPlaceArea() {
		return getObjectTemplate().getArea();
	}

	public int getPlacementLimit(boolean trial) {
		LimitType limitType = getObjectTemplate().getPlacementLimit();
		if (trial)
			return limitType.getTrialObjectPlaceLimit(registry.getOwner().getBuilding().getSize());
		return limitType.getObjectPlaceLimit(registry.getOwner().getBuilding().getSize());
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

	public HouseRegistry getRegistry() {
		return registry;
	}

	public House getOwnerHouse() {
		return registry.getOwner();
	}

	public int getPlayerId() {
		return registry.getOwner().getOwnerId();
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
		Player owner = World.getInstance().getPlayer(registry.getOwner().getOwnerId());
		// if owner is not online, we should save his items
		if (owner == null || !owner.isOnline())
			registry.save();
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
		if (position == null || !isSpawned()) {
			position = World.getInstance().createPosition(registry.getOwner().getWorldId(), x, y, z, heading, registry.getOwner().getInstanceId());
			SpawnEngine.bringIntoWorld(this);
		} else {
			updateKnownlist();
		}
	}

	/**
	 * Removes house from spawn but it remains in registry
	 */
	public void removeFromHouse() {
		getController().delete();
		x = y = z = heading = 0;
		position = null;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
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
