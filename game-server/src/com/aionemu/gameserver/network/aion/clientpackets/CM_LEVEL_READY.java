package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.configs.main.EventsConfig;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.animations.ArrivalAnimation;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.FlyState;
import com.aionemu.gameserver.model.items.storage.StorageType;
import com.aionemu.gameserver.model.templates.windstreams.Location2D;
import com.aionemu.gameserver.model.templates.windstreams.WindstreamTemplate;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.*;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.services.SiegeService;
import com.aionemu.gameserver.services.TownService;
import com.aionemu.gameserver.services.WeatherService;
import com.aionemu.gameserver.services.conquerorAndProtectorSystem.ConquerorAndProtectorService;
import com.aionemu.gameserver.services.event.EventService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.rift.RiftInformer;
import com.aionemu.gameserver.world.World;

/**
 * Client is saying that level[map] is ready.
 *
 * @author -Nemesiss-, Kwazar
 */
public class CM_LEVEL_READY extends AionClientPacket {

	public CM_LEVEL_READY(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
	}

	@Override
	protected void runImpl() {
		Player activePlayer = getConnection().getActivePlayer();

		if (activePlayer.getActiveHouse() != null)
			sendPacket(new SM_HOUSE_OBJECTS(activePlayer.getActiveHouse().getRegistry().getSpawnedObjects()));
		if (activePlayer.isInInstance()) {
			sendPacket(new SM_INSTANCE_COUNT_INFO(activePlayer.getWorldId(), activePlayer.getInstanceId()));
		}
		sendPacket(new SM_PLAYER_INFO(activePlayer));
		activePlayer.getController().startProtectionActiveTask();
		sendPacket(new SM_ACCOUNT_PROPERTIES());
		sendPacket(new SM_MOTION(activePlayer.getObjectId(), activePlayer.getMotions().getActiveMotions()));

		WindstreamTemplate template = DataManager.WINDSTREAM_DATA.getStreamTemplate(activePlayer.getPosition().getMapId());
		if (template != null)
			for (Location2D location : template.getLocations().getLocation()) {
				sendPacket(new SM_WINDSTREAM_ANNOUNCE(location.getFlyPathType().getId(), template.getMapId(), location.getId(), location.getState()));
			}

		// Spawn player into the world.
		World.getInstance().spawn(activePlayer);

		if (activePlayer.isInFlyState(FlyState.FLYING)) // notify client if we are still flying (client always ends flying after teleport)
			activePlayer.getFlyController().startFly(true, true);

		// SM_SHIELD_EFFECT, SM_ABYSS_ARTIFACT_INFO3
		if (activePlayer.isInSiegeWorld()) {
			SiegeService.getInstance().onEnterSiegeWorld(activePlayer);
		}

		// SM_CONQUEROR_PROTECTOR
		ConquerorAndProtectorService.getInstance().onEnterMap(activePlayer);

		// SM_RIFT_ANNOUNCE
		RiftInformer.sendRiftsInfo(activePlayer);

		// SM_UPGRADE_ARCADE
		if (EventsConfig.ENABLE_EVENT_ARCADE)
			sendPacket(new SM_UPGRADE_ARCADE(true));

		// SM_NEARBY_QUESTS
		activePlayer.getController().updateNearbyQuests();

		// SM_QUEST_REPEAT
		activePlayer.getController().updateRepeatableQuests();

		// Loading weather for the player's region
		WeatherService.getInstance().loadWeather(activePlayer);

		QuestEngine.getInstance().onEnterWorld(activePlayer);

		activePlayer.getController().onEnterWorld();
		InstanceService.onEnterInstance(activePlayer);
		activePlayer.getEffectController().updatePlayerEffectIcons(null);
		sendPacket(SM_CUBE_UPDATE.cubeSize(StorageType.CUBE, activePlayer));

		Pet pet = activePlayer.getPet();
		if (pet != null && !pet.isSpawned())
			World.getInstance().spawn(pet);
		activePlayer.setPortAnimation(ArrivalAnimation.NONE);

		TownService.getInstance().onEnterWorld(activePlayer);
		EventService.getInstance().onEnterMap(activePlayer);
	}
}
