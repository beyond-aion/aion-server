package com.aionemu.gameserver.network.aion.clientpackets;

import java.util.Set;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.AionClientPacket;
import com.aionemu.gameserver.network.aion.AionConnection.State;
import com.aionemu.gameserver.services.item.ItemPurificationService;

/**
 * @author FinalNovas
 * @reworked Navyan
 */
public class CM_ITEM_PURIFICATION extends AionClientPacket {

	@SuppressWarnings("unused")
	private int playerObjectId, requireItemObjectId1, requireItemObjectId2, requireItemObjectId3, requireItemObjectId4, requireItemObjectId5;
	private int upgradedItemObjectId;
	private int resultItemId;

	/**
	 * @param opcode
	 */
	public CM_ITEM_PURIFICATION(int opcode, Set<State> validStates) {
		super(opcode, validStates);
	}

	@Override
	protected void readImpl() {
		playerObjectId = readD();
		upgradedItemObjectId = readD();
		resultItemId = readD();
		requireItemObjectId1 = readD();
		requireItemObjectId2 = readD();
		requireItemObjectId3 = readD();
		requireItemObjectId4 = readD();
		requireItemObjectId5 = readD();
	}

	@Override
	protected void runImpl() {
		Player player = getConnection().getActivePlayer();
		if (player == null)
			return;

		Item baseItem = player.getInventory().getItemByObjId(upgradedItemObjectId);
		if (!ItemPurificationService.checkItemUpgrade(player, baseItem, resultItemId))
			return;

		if (!ItemPurificationService.decreaseMaterial(player, baseItem, resultItemId))
			return;

		ItemPurificationService.upgradeItem(player, baseItem, resultItemId);
	}
}
