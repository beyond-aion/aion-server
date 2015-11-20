package ai;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AI2Request;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.templates.housing.BuildingType;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_TELEPORT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author xTz, Rolandas
 */
@AIName("housegate")
public class HouseGateAI2 extends NpcAI2 {

	@Override
	protected void handleDialogStart(Player player) {
		final int creatorId = getCreatorId();
		// Only group member and creator may use gate
		if (!player.getObjectId().equals(creatorId)) {
			if (player.getCurrentGroup() == null || !player.getCurrentGroup().hasMember(creatorId))
				return;
		}

		House house = HousingService.getInstance().getPlayerStudio(creatorId);
		if (house == null) {
			int address = HousingService.getInstance().getPlayerAddress(creatorId);
			house = HousingService.getInstance().getHouseByAddress(address);
		}
		// Uses skill but doesn't have house
		if (house == null)
			return;

		if (house.getLevelRestrict() > player.getLevel())
			// msg
			return;
		boolean returnBattle = true;
		for (ZoneInstance zone : player.getPosition().getMapRegion().getZones(player)) {
			if (!zone.canReturnToBattle()) {
				returnBattle = false;
				break;
			}
		}
		int requestId = SM_QUESTION_WINDOW.STR_ASK_GROUP_GATE_DO_YOU_ACCEPT_MOVE;
		if (!returnBattle)
			requestId = SM_QUESTION_WINDOW.STR_HOUSE_GATE_ACCEPT_MOVE_DONT_RETURN;

		AI2Actions.addRequest(this, player, requestId, 0, 9, new AI2Request() {

			private boolean decided = false;

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				if (decided)
					return;

				House house = HousingService.getInstance().getPlayerStudio(creatorId);
				if (house == null) {
					int address = HousingService.getInstance().getPlayerAddress(creatorId);
					house = HousingService.getInstance().getHouseByAddress(address);
				}
				int instanceOwnerId = responder.getPosition().getWorldMapInstance().getOwnerId();

				int exitMapId = 0;
				float x = 0, y = 0, z = 0;
				byte heading = 0;
				int instanceId = 0;

				if (instanceOwnerId > 0) { // leaving
					house = HousingService.getInstance().getPlayerStudio(instanceOwnerId);
					exitMapId = house.getAddress().getExitMapId();
					instanceId = World.getInstance().getWorldMap(exitMapId).getMainWorldMapInstance().getInstanceId();
					x = house.getAddress().getExitX();
					y = house.getAddress().getExitY();
					z = house.getAddress().getExitZ();
				} else { // entering house
					exitMapId = house.getAddress().getMapId();
					if (house.getBuilding().getType() == BuildingType.PERSONAL_INS) { // entering studio
						WorldMapInstance instance = InstanceService.getPersonalInstance(exitMapId, creatorId);
						if (instance == null) {
							instance = InstanceService.getNextAvailableInstance(exitMapId, creatorId);
							InstanceService.registerPlayerWithInstance(instance, responder);
						}
						instanceId = instance.getInstanceId();
					} else { // entering ordinary house
						instanceId = house.getInstanceId();
					}
					x = house.getAddress().getX();
					y = house.getAddress().getY();
					z = house.getAddress().getZ();
					if (exitMapId == 710010000) {
						heading = 36;
					}
				}
				boolean canReturnToBattle = true;
				for (ZoneInstance zone : responder.getPosition().getMapRegion().getZones(responder)) {
					if (!zone.canReturnToBattle()) {
						canReturnToBattle = false;
						break;
					}
				}
				if (!canReturnToBattle) {
					responder.setBattleReturnCoords(0, null);
				} else {
					PacketSendUtility.sendPacket(responder, new SM_HOUSE_TELEPORT(house.getAddress().getId(), responder.getObjectId()));
					responder.setBattleReturnCoords(responder.getWorldId(), new float[] { responder.getX(), responder.getY(), responder.getZ() });
				}
				TeleportService2.teleportTo(responder, exitMapId, instanceId, x, y, z + 1.5f, heading, TeleportAnimation.JUMP_IN_GATE);
				decided = true;
			}

			@Override
			public void denyRequest(Creature requester, Player responder) {
				decided = true;
			}

		});

	}

}
