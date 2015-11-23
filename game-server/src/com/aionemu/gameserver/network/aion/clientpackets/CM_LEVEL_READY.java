package com.aionemu.gameserver.network.aion.clientpackets;

import com.aionemu.gameserver.configs.main.AutoGroupConfig;
import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.ArrivalAnimation;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.windstreams.Location2D;
import com.aionemu.gameserver.model.templates.windstreams.WindstreamTemplate;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ACCOUNT_PROPERTIES;
import com.aionemu.gameserver.network.aion.serverpackets.SM_CUBE_UPDATE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_OBJECTS;
import com.aionemu.gameserver.network.aion.serverpackets.SM_INSTANCE_COUNT_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_MOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAYER_INFO;
import com.aionemu.gameserver.network.aion.serverpackets.SM_UPGRADE_ARCADE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_WINDSTREAM_ANNOUNCE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.AutoGroupService;
import com.aionemu.gameserver.services.SerialKillerService;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.TownService;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.services.rift.RiftInformer;
import com.aionemu.gameserver.spawnengine.InstanceRiftSpawnManager;
import com.aionemu.gameserver.world.World;

/**
 * Client is saying that level[map] is ready.
 *
 * @author -Nemesiss-
 * @author Kwazar
 */
public class CM_LEVEL_READY extends AionClientPacket {

	public CM_LEVEL_READY(int opcode, State state, State... restStates) {
		super(opcode, state, restStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();

		if (!activePlayer.getCommonData().isOnline())
			activePlayer.getCommonData().setOnline(true);

		if (activePlayer.getHouseRegistry() != null)
			sendPacket(new SM_HOUSE_OBJECTS(activePlayer));
		if (activePlayer.isInInstance()) {
			sendPacket(new SM_INSTANCE_COUNT_INFO(activePlayer.getWorldId(), activePlayer.getInstanceId()));
		}
		sendPacket(new SM_PLAYER_INFO(activePlayer, false));
		activePlayer.getController().startProtectionActiveTask();
		sendPacket(new SM_ACCOUNT_PROPERTIES());
		sendPacket(new SM_MOTION(activePlayer.getObjectId(), activePlayer.getMotions().getActiveMotions()));

		WindstreamTemplate template = DataManager.WINDSTREAM_DATA.getStreamTemplate(activePlayer.getPosition().getMapId());
		Location2D location;
		if (template != null)
			for (int i = 0; i < template.getLocations().getLocation().size(); i++) {
				location = template.getLocations().getLocation().get(i);
				sendPacket(new SM_WINDSTREAM_ANNOUNCE(location.getFlyPathType().getId(), template.getMapid(), location.getId(), location.getState()));
			}
		location = null;
		template = null;

		/**
		 * Spawn player into the world.
		 */
		// If already spawned, despawn before spawning into the world
		if (activePlayer.isSpawned())
			World.getInstance().despawn(activePlayer);
		World.getInstance().spawn(activePlayer);

		activePlayer.getController().refreshZoneImpl();

		// SM_SHIELD_EFFECT, SM_ABYSS_ARTIFACT_INFO3
		if (activePlayer.isInSiegeWorld()) {
			SiegeService.getInstance().onEnterSiegeWorld(activePlayer);
		}

		// SM_SERIAL_KILLER
		SerialKillerService.getInstance().onEnterMap(activePlayer);

		// SM_RIFT_ANNOUNCE
		RiftInformer.sendRiftsInfo(activePlayer);
		InstanceRiftSpawnManager.sendInstanceRiftStatus(activePlayer);

		// SM_AUTO_GROUP's
		if (AutoGroupConfig.AUTO_GROUP_ENABLE) {
			AutoGroupService.getInstance().onEnterWorld(activePlayer);
		}

		// SM_UPGRADE_ARCADE
		if (EventsConfig.ENABLE_EVENT_ARCADE)
			sendPacket(new SM_UPGRADE_ARCADE(true));

		// SM_NEARBY_QUESTS
		activePlayer.getController().updateNearbyQuests();

		// SM_QUEST_REPEAT
		// activePlayer.getController().updateRepeatableQuests();

		/**
		 * Loading weather for the player's region
		 */
		WeatherService.getInstance().loadWeather(activePlayer);

		QuestEngine.getInstance().onEnterWorld(new QuestEnv(null, activePlayer, 0, 0));

		activePlayer.getController().onEnterWorld();
		activePlayer.getEffectController().updatePlayerEffectIcons(null);
		sendPacket(SM_CUBE_UPDATE.cubeSize(StorageType.CUBE, activePlayer));

		Pet pet = activePlayer.getPet();
		if (pet != null && !pet.isSpawned())
			World.getInstance().spawn(pet);
		activePlayer.setPortAnimation(ArrivalAnimation.NONE);

		TownService.getInstance().onEnterWorld(activePlayer);
	}

}
