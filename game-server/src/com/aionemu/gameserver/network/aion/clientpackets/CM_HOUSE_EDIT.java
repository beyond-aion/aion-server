package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.HouseDecoration;
import com.aionemu.gameserver.model.gameobjects.HouseObject;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Persistable.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.house.House;
import com.aionemu.gameserver.model.house.HousingCommand;
import com.aionemu.gameserver.model.house.HousingContainer;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.DecorateAction;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_EDIT;
import com.aionemu.gameserver.network.aion.serverpackets.SM_HOUSE_REGISTRY;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.HousingService;
import com.aionemu.gameserver.services.item.HouseObjectFactory;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemDeleteType;
import com.aionemu.gameserver.utils.audit.AuditLogger;
import com.aionemu.gameserver.utils.idfactory.IDFactory;

/**
 * @author Rolandas
 */
public class CM_HOUSE_EDIT extends AionClientPacket {

	private HousingCommand action;
	private int itemObjectId;
	private float x, y, z;
	private int rotation;
	private int buildingId;

	public CM_HOUSE_EDIT(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		int actionId = readUC();
		action = HousingCommand.getById(actionId);
		if (action == null) {
			LoggerFactory.getLogger(CM_HOUSE_EDIT.class).warn("{} sent unknown house edit action {}", getConnection().getActivePlayer(), actionId);
			return;
		}
		switch (action) {
			case REGISTER_ITEM, REMOVE_ITEM, WITHDRAW_OBJECT -> itemObjectId = readD();
			case PUT_OBJECT, MOVE_OBJECT -> {
				itemObjectId = readD();
				x = readF();
				y = readF();
				z = readF();
				rotation = readUH();
			}
			case CHANGE_BUILDING -> buildingId = readD();
		}
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null || action == null)
			return;
		House house = player.getActiveHouse();
		if (house == null)
			return;
		if (action != HousingCommand.END_EDIT && action != HousingCommand.END_BUILDING_EDIT && !house.isOnLand(player))
			return;
		if ((action == HousingCommand.PUT_OBJECT || action == HousingCommand.MOVE_OBJECT) && !house.isInsideLand(x, y)) {
			AuditLogger.log(player, "tried to place a house object at " + x + ", " + y + ", " + z + " outside of house " + house.getAddress().getId());
			return;
		}

		switch (action) {
			case BEGIN_EDIT -> {
				sendPacket(new SM_HOUSE_EDIT(action));
				sendPacket(new SM_HOUSE_REGISTRY(HousingContainer.OBJECTS));
				sendPacket(new SM_HOUSE_REGISTRY(HousingContainer.DECORATIONS));
			}
			case END_EDIT, BEGIN_BUILDING_EDIT, END_BUILDING_EDIT -> sendPacket(new SM_HOUSE_EDIT(action));
			case REGISTER_ITEM -> {
				Item item = player.getInventory().getItemByObjId(itemObjectId);
				if (item == null)
					return;

				ItemTemplate template = item.getItemTemplate();
				player.getInventory().delete(item, ItemDeleteType.REGISTER);

				DecorateAction decorateAction = template.getActions().getDecorateAction();
				if (decorateAction != null) {
					HouseDecoration decor = new HouseDecoration(IDFactory.getInstance().nextId(), decorateAction.getTemplateId());
					house.getRegistry().putDecor(decor, true);
					sendPacket(new SM_HOUSE_EDIT(action, HousingContainer.DECORATIONS, decor.getObjectId()));
				} else {
					HouseObject<?> obj = HouseObjectFactory.createNew(house, template);
					house.getRegistry().putObject(obj, true);
					sendPacket(new SM_HOUSE_EDIT(action, HousingContainer.OBJECTS, obj.getObjectId()));
				}
			}
			case REMOVE_ITEM -> {
				house.getRegistry().discardObject(house.getRegistry().getObjectByObjId(itemObjectId), false);
				sendPacket(new SM_HOUSE_EDIT(action, HousingContainer.OBJECTS, itemObjectId));
				sendPacket(new SM_HOUSE_EDIT(action, HousingContainer.OBJECTS, itemObjectId));
			}
			case PUT_OBJECT -> {
				HouseObject<?> obj = house.getRegistry().getObjectByObjId(itemObjectId);
				if (obj == null)
					return;
				obj.setX(x);
				obj.setY(y);
				obj.setZ(z);
				obj.setRotation(rotation);
				sendPacket(new SM_HOUSE_EDIT(action, itemObjectId, x, y, z, rotation));
				obj.spawn();
				house.getRegistry().setPersistentState(PersistentState.UPDATE_REQUIRED);
				sendPacket(new SM_HOUSE_EDIT(HousingCommand.REMOVE_ITEM, HousingContainer.OBJECTS, itemObjectId));
				QuestEngine.getInstance().onHouseItemUseEvent(new QuestEnv(null, player, 0));
			}
			case MOVE_OBJECT -> {
				HouseObject<?> obj = house.getRegistry().getObjectByObjId(itemObjectId);
				if (obj == null)
					return;
				sendPacket(new SM_HOUSE_EDIT(HousingCommand.WITHDRAW_OBJECT, itemObjectId));
				obj.getController().delete();
				obj.setX(x);
				obj.setY(y);
				obj.setZ(z);
				obj.setRotation(rotation);
				if (obj.getPersistentState() == PersistentState.UPDATE_REQUIRED)
					house.getRegistry().setPersistentState(PersistentState.UPDATE_REQUIRED);
				sendPacket(new SM_HOUSE_EDIT(HousingCommand.PUT_OBJECT, itemObjectId, x, y, z, rotation));
				obj.spawn();
			}
			case WITHDRAW_OBJECT -> {
				HouseObject<?> obj = house.getRegistry().getObjectByObjId(itemObjectId);
				if (obj == null)
					return;
				sendPacket(new SM_HOUSE_EDIT(action, itemObjectId));
				obj.removeFromHouse();
				sendPacket(new SM_HOUSE_EDIT(HousingCommand.REGISTER_ITEM, HousingContainer.OBJECTS, itemObjectId)); // place it back
			}
			case CHANGE_BUILDING -> {
				if (house.getLand().getBuildings().stream().noneMatch(building -> building.getId() == buildingId)) {
					AuditLogger.log(player, "attempted house renovation to building " + buildingId + " which doesn't belong to " + house.getLand().getId());
					return;
				}
				if (!removeRenovationCoupon(player, house)) {
					AuditLogger.log(player, "attempted house renovation without coupon");
					return;
				}
				HousingService.getInstance().switchHouseBuilding(house, buildingId);
				house.getController().updateAppearance();
			}
		}
	}

	private boolean removeRenovationCoupon(Player player, House house) {
		int typeId = house.getHouseType().getId();
		if (typeId == 0)
			return false; // studio
		int itemId = (player.getRace().equals(Race.ELYOS) ? 169661004 : 169661008) - typeId;
		if (player.getInventory().getItemCountByItemId(itemId) > 0)
			return player.getInventory().decreaseByItemId(itemId, 1);
		return false;
	}
}
