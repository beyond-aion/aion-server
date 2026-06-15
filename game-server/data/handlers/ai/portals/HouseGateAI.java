package ai.portals;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.AIRequest;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_TELEPORT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_QUESTION_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author xTz, Rolandas
 */
@AIName("housegate")
public class HouseGateAI extends NpcAI {

	public HouseGateAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		final int creatorId = getCreatorId();
		boolean isCreatorOrFriend = player.equals(getCreator()) || player.getFriendList().getFriend(creatorId) != null
			|| player.isInGroup() && player.getCurrentGroup().hasMember(creatorId);
		if (!isCreatorOrFriend) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_HOUSING_TELEPORT_CANT_USE());
			return;
		}

		House house = HousingService.getInstance().findActiveHouse(creatorId);
		// Uses skill but doesn't have house
		if (house == null) {
			if (player.getObjectId() == creatorId)
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_HOUSING_TELEPORT_NEED_HOUSE());
			else
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_HOUSING_TELEPORT_CANT_USE());
			return;
		}

		if (!player.getCommonData().isDaeva()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_ENTER_NO_RIGHT(house.getAddress().getId()));
			return;
		}

		if (!house.canEnter(player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_HOUSING_CANT_ENTER_NO_RIGHT2());
			return;
		}
		boolean returnBattle = true;
		for (ZoneInstance zone : player.findZones()) {
			if (!zone.canReturnToBattle()) {
				returnBattle = false;
				break;
			}
		}
		int requestId = SM_QUESTION_WINDOW.STR_ASK_GROUP_GATE_DO_YOU_ACCEPT_MOVE;
		if (!returnBattle)
			requestId = SM_QUESTION_WINDOW.STR_HOUSE_GATE_ACCEPT_MOVE_DONT_RETURN;

		AIActions.addRequest(this, player, requestId, 9, new AIRequest() {

			private boolean decided = false;

			@Override
			public void acceptRequest(Creature requester, Player responder, int requestId) {
				if (decided)
					return;

				WorldMapInstance instance = InstanceService.getOrCreateHouseInstance(house);
				boolean canReturnToBattle = true;
				for (ZoneInstance zone : responder.findZones()) {
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
				TeleportService.teleportTo(responder, instance, house.getX(), house.getY(), house.getZ(), house.getTeleportHeading(),
					TeleportAnimation.JUMP_IN_GATE);
				decided = true;
			}

			@Override
			public void denyRequest(Creature requester, Player responder) {
				decided = true;
			}

		});

	}

}
