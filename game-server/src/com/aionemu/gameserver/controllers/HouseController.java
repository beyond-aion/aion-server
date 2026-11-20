package com.aionemu.gameserver.controllers;

import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.VisibleObject;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.HouseAddress;
import com.aionemu.gameserver.model.templates.housing.HouseType;
import com.aionemu.gameserver.model.templates.spawns.HouseSpawn;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.model.templates.spawns.SpawnType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.PositionUtil;
import com.aionemu.gameserver.world.geo.GeoService;

/**
 * @author Rolandas, Neon
 */
public class HouseController extends VisibleObjectController<House> {

	private static final Logger log = LoggerFactory.getLogger(HouseController.class);

	@Override
	public void see(VisibleObject object) {
		if (object instanceof Player)
			spawnObjects();
	}

	public void spawnObjects() {
		if (getOwner().getPosition() != null && getOwner().isSpawned() && !getOwner().isInactive()) {
			for (HouseObject<?> obj : getOwner().getRegistry().getSpawnedObjects())
				obj.spawn();
		}
	}

	@Override
	public void onAfterSpawn() {
		// loads scripts and registry from DB if not already initialized
		getOwner().getPlayerScripts();
		getOwner().getRegistry();
		updateSpawns();
		GeoService.getInstance().setHouseDoorState(getOwner().getWorldId(), getOwner().getInstanceId(), getOwner().getAddress().getId(), getOwner().getDoorState());
	}

	private void updateSpawns() {
		HouseAddress address = getOwner().getAddress();
		List<HouseSpawn> templates = DataManager.HOUSE_NPCS_DATA.getSpawnsByAddress(address.getId());
		if (templates == null) {
			log.warn("Missing npc spawns for house {}", address.getId());
			return;
		}
		for (HouseSpawn spawn : templates) {
			Npc npc;
			if (spawn.getType() == SpawnType.MANAGER || spawn.getType() == SpawnType.TELEPORT) {
				npc = hasButlerAndCrystal() ? spawnButlerOrCrystal(spawn) : null;
			} else if (spawn.getType() == SpawnType.SIGN) {
				// Signs do not have master name displayed, but have creatorId
				int creatorId = address.getId();
				SpawnTemplate t = SpawnEngine.newSingleTimeSpawn(address.getMapId(), getCurrentSignNpcId(), spawn.getX(), spawn.getY(), spawn.getZ(),
					spawn.getH(), creatorId);
				npc = (Npc) SpawnEngine.spawnObject(t, getOwner().getInstanceId());
			} else {
				log.warn("Unhandled spawn type {}", spawn.getType());
				continue;
			}
			getOwner().updateSpawn(spawn.getType(), npc);
		}
	}

	@Override
	public void onDespawn() {
		super.onDespawn();
		boolean isReusableStudio = HousingService.getInstance().findStudio(getOwner().getObjectId()) == getOwner();
		if (isReusableStudio) { // save studio and release despawned npcs and the destroyed mapregion / worldmapinstance, since studio stays in RAM
			getOwner().save();
			getOwner().clearSpawns();
			getOwner().setPosition(null);
		}
	}

	public void updateAppearance() {
		PacketSendUtility.broadcastPacket(getOwner(), new SM_HOUSE_UPDATE(getOwner()));
	}

	public void kickVisitors(Player kicker, boolean kickFriends, boolean ownerChanged) {
		String houseZone = getOwner().getName();
		getOwner().getKnownList().forEachPlayer(player -> {
			if (player.getObjectId() == getOwner().getOwnerId())
				return;
			if (!kickFriends && kicker != null && kicker.getFriendList().getFriend(player.getObjectId()) != null)
				return;
			if (player.isInsideZone(houseZone))
				moveOutside(player, ownerChanged);
		});
		if (kicker != null) {
			if (!kickFriends) {
				PacketSendUtility.sendPacket(kicker, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OUT_WITHOUT_FRIENDS());
			} else {
				PacketSendUtility.sendPacket(kicker, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_ORDER_OUT_ALL());
			}
		}
	}

	private void moveOutside(Player player, boolean ownerChanged) {
		if (getOwner().getAddress().getExitMapId() != null) {
			HouseAddress address = getOwner().getAddress();
			TeleportService.teleportTo(player, address.getExitMapId(), address.getExitX(), address.getExitY(), address.getExitZ(), (byte) 0,
				TeleportAnimation.FADE_OUT_BEAM);
		} else {
			teleportNearHouseDoor(player, true);
		}
		if (ownerChanged)
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CHANGE_OWNER());
		else
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_REQUEST_OUT());
	}

	@SuppressWarnings("lossy-conversions")
	public void teleportNearHouseDoor(Player player, boolean outsideHouse) {
		HouseSpawn butler = getOwner().getSpawnTemplate(SpawnType.MANAGER), relationshipCrystal = getOwner().getSpawnTemplate(SpawnType.TELEPORT);
		float x, y, z; // midpoint between butler and relationship crystal, since we currently have no door coordinates in templates
		byte h = PositionUtil.getHeadingTowards(relationshipCrystal.getX(), relationshipCrystal.getY(), butler.getX(), butler.getY());
		h -= 30; // this is the supposed heading towards the door (crystal is right from the door, so offset direction towards butler by 90 degrees)
		x = (butler.getX() + relationshipCrystal.getX()) / 2;
		y = (butler.getY() + relationshipCrystal.getY()) / 2;
		z = Math.max(butler.getZ(), relationshipCrystal.getZ());
		if (outsideHouse) { // offset the midpoint 2.5m behind the butler, to get coords outside the house, near the door
			double radian = Math.toRadians(PositionUtil.convertHeadingToAngle(h));
			x += (float) (Math.cos(radian) * 2.5f);
			y += (float) (Math.sin(radian) * 2.5f);
		} else {
			h += h < 60 ? 60 : -60; // opposite direction (player should look inside house)
		}
		TeleportService.teleportTo(player, getOwner().getWorldId(), getOwner().getInstanceId(), x, y, z, h, TeleportAnimation.FADE_OUT_BEAM);
	}

	/**
	 * Retail (NPC server HousingDb::SetFieldHouse) spawns the butler and relationship crystal for a house on auction (state 3) and for an owned
	 * house (state 5), but not for an unowned one (state 2) or one whose buyer still owns another house (state 4, our inactive house).
	 */
	private boolean hasButlerAndCrystal() {
		House house = getOwner();
		return house.getBids() != null || house.getOwnerId() != 0 && !house.isInactive();
	}

	private Npc spawnButlerOrCrystal(HouseSpawn spawn) {
		HouseAddress address = getOwner().getAddress();
		int npcId = spawn.getType() == SpawnType.MANAGER ? address.getLand().getManagerNpcId() : address.getLand().getTeleportNpcId();
		SpawnTemplate t = SpawnEngine.newSingleTimeSpawn(address.getMapId(), npcId, spawn.getX(), spawn.getY(), spawn.getZ(), spawn.getH());
		return VisibleObjectSpawner.spawnHouseNpc(t, getOwner().getInstanceId(), getOwner());
	}

	private void updateButlerAndCrystal() {
		boolean spawned = getOwner().getButler() != null;
		if (spawned == hasButlerAndCrystal())
			return;
		for (SpawnType type : new SpawnType[] { SpawnType.MANAGER, SpawnType.TELEPORT })
			getOwner().updateSpawn(type, spawned ? null : spawnButlerOrCrystal(getOwner().getSpawnTemplate(type)));
	}

	/**
	 * Updates the sign, butler and relationship crystal after the auction or activity state of the house changed.
	 */
	public void updateSign() {
		if (getOwner().getCurrentSign() == null)
			return;
		updateButlerAndCrystal();
		int newNpcId = getCurrentSignNpcId();
		if (newNpcId != getOwner().getCurrentSign().getNpcId()) {
			SpawnTemplate t = getOwner().getCurrentSign().getSpawn();
			t = SpawnEngine.newSingleTimeSpawn(t.getWorldId(), newNpcId, t.getX(), t.getY(), t.getZ(), t.getHeading(), t.getCreatorId());
			getOwner().updateSpawn(SpawnType.SIGN, (Npc) SpawnEngine.spawnObject(t, getOwner().getInstanceId()));
		}
	}

	public void updateHouseSpawns() {
		// only update spawns in active studios
		if (getOwner().getHouseType() == HouseType.STUDIO && (getOwner().getPosition() == null || !getOwner().isSpawned()))
			return;
		getOwner().updateSpawn(SpawnType.MANAGER, null); // remove old butler, otherwise new npcs spawn with old owner name
		updateSpawns();
		updateAppearance();
	}

	private int getCurrentSignNpcId() {
		if (getOwner().getBids() != null)
			return getOwner().getLand().getSaleSignNpcId();
		if (getOwner().getOwnerId() == 0)
			return getOwner().getLand().getNosaleSignNpcId(); // invisible npc
		return getOwner().isInactive() ? getOwner().getLand().getWaitingSignNpcId() : getOwner().getLand().getHomeSignNpcId();
	}
}
