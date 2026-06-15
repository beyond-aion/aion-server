package com.aionemu.gameserver.model.templates.item.actions;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Kisk;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.KiskService;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.spawnengine.VisibleObjectSpawner;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Sarynth, Source
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ToyPetSpawnAction")
public class ToyPetSpawnAction extends AbstractItemAction {

	@XmlAttribute
	protected int npcid;

	@XmlAttribute
	protected int time;

	public int getNpcId() {
		return npcid;
	}

	public int getTime() {
		return time;
	}

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (player.isFlying()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_BINDSTONE_ITEM_WHILE_FLYING());
			return false;
		}
		if (player.isInInstance()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_REGISTER_BINDSTONE_FAR_FROM_NPC());
			return false;
		}
		if (KiskService.getInstance().haveKisk(player.getObjectId()) && CustomConfig.ENABLE_KISK_RESTRICTION) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_BINDSTONE_ALREADY_INSTALLED());
			return false;
		}
		if (!isPutKiskZone(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_CANNOT_USE_ITEM_INVALID_LOCATION());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, Item targetItem, Object... params) {
		// ShowAction
		player.getController().cancelUseItem();
		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 10000, 0, 0), true);
		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_CANCELED());
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 2, 0), true);
			}
		};

		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(() -> {
			PacketSendUtility.broadcastPacket(player,
				new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, 1, 1), true);
			player.getObserveController().removeObserver(observer);
			// RemoveKisk
			if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1))
				return;
			float x = player.getX();
			float y = player.getY();
			float z = player.getZ();
			byte heading = (byte) ((player.getHeading() + 60) % 120);
			int worldId = player.getWorldId();
			int instanceId = player.getInstanceId();
			SpawnTemplate spawn = SpawnEngine.newSingleTimeSpawn(worldId, npcid, x, y, z, heading);

			final Kisk kisk = VisibleObjectSpawner.spawnKisk(spawn, instanceId, player);
			final int objOwnerId = player.getObjectId();
			// Schedule Despawn Action
			Future<?> task = ThreadPoolManager.getInstance().schedule(() -> kisk.getController().delete(), kisk.getRemainingLifetime(), TimeUnit.SECONDS);
			kisk.getController().addTask(TaskId.DESPAWN, task);

			// ShowFinalAction
			player.getController().cancelTask(TaskId.ITEM_USE);
			KiskService.getInstance().regKisk(kisk, objOwnerId);

			if (kisk.getMaxMembers() > 1)
				kisk.getController().onDialogRequest(player);
			else
				KiskService.getInstance().onBind(kisk, player);
		}, 10000));
	}

	private boolean isPutKiskZone(Player player) {
		for (ZoneInstance zone : player.findZones()) {
			if (!zone.canPutKisk())
				return false;
		}
		return true;
	}
}
