package com.aionemu.gameserver.services.item;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.templates.item.actions.TuningAction;
import com.aionemu.gameserver.model.templates.item.bonuses.StatBonusType;
import com.aionemu.gameserver.model.templates.item.purification.ItemPurificationTemplate;
import com.aionemu.gameserver.model.templates.item.purification.PurificationResult;
import com.aionemu.gameserver.model.templates.item.purification.RequiredMaterial;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Ranastic, Estrayl
 */
public class ItemPurificationService {

	private static final Logger log = LoggerFactory.getLogger(ItemPurificationService.class);

	public static boolean isPurificationAllowed(Player player, Item baseItem, int resultItemId) {
		ItemPurificationTemplate itemPurificationTemplate = DataManager.ITEM_PURIFICATION_DATA.getItemPurificationTemplate(baseItem.getItemId());
		if (itemPurificationTemplate == null) {
			log.warn("Item purification template is not available for [resultItemId=" + resultItemId + "]");
			return false;
		}

		Map<Integer, PurificationResult> resultItemMap = DataManager.ITEM_PURIFICATION_DATA.getResultItemMap(baseItem.getItemId());
		PurificationResult purificationResult = resultItemMap.get(resultItemId);
		if (purificationResult == null) {
			AuditLogger.log(player,
				"tried to purify an item to an invalid result [baseItemId=" + baseItem.getItemId() + ", resultItemId=" + resultItemId + "]");
			return false;
		}

		if (!baseItem.isIdentified()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NO_IDENTIFY());
			return false;
		}

		if (baseItem.getEnchantLevel() < purificationResult.getMinEnchantCount()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT(baseItem.getL10n()));
			return false;
		}

		if (player.getAbyssRank().getAp() < purificationResult.getNecessaryAbyssPoints()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NEED_AP());
			return false;
		}

		if (player.getInventory().getKinah() < purificationResult.getNecessaryKinah()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NEED_QINA());
			return false;
		}

		for (RequiredMaterial reqMat : purificationResult.getRequiredMaterials())
			if (player.getInventory().getItemCountByItemId(reqMat.getItemId()) < reqMat.getItemCount())
				return false;

		String resultItemL10n = DataManager.ITEM_DATA.getItemTemplate(resultItemId).getL10n();
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_UPGRADE_MSG_UPGRADE_SUCCESS(baseItem.getL10n(), resultItemL10n));
		return true;
	}

	public static boolean decreaseMaterials(Player player, Item baseItem, int resultItemId) {
		Map<Integer, PurificationResult> resultItemMap = DataManager.ITEM_PURIFICATION_DATA.getResultItemMap(baseItem.getItemId());

		PurificationResult purificationResult = resultItemMap.get(resultItemId);

		for (RequiredMaterial reqMaterial : purificationResult.getRequiredMaterials()) {
			if (!player.getInventory().decreaseByItemId(reqMaterial.getItemId(), reqMaterial.getItemCount())) {
				AuditLogger.log(player, "tried to use item purification with insufficient materials [baseItemId=" + baseItem.getItemId() + ", resultItemId="
					+ resultItemId + ", reqMaterialId=" + reqMaterial.getItemId() + ", reqMaterialCount=" + reqMaterial.getItemCount() + "]");
				return false;
			}
		}

		if (purificationResult.getNecessaryAbyssPoints() > 0)
			AbyssPointsService.addAp(player, -purificationResult.getNecessaryAbyssPoints());

		if (purificationResult.getNecessaryKinah() > 0)
			player.getInventory().decreaseKinah(-purificationResult.getNecessaryKinah());

		player.getInventory().decreaseByObjectId(baseItem.getObjectId(), 1);

		return true;
	}

	public static void upgradeItem(Player player, Item sourceItem, int targetItemId) {
		Item newItem = ItemFactory.newItem(targetItemId, 1);
		newItem.setOptionalSockets(sourceItem.getOptionalSockets());
		newItem.setItemCreator(sourceItem.getItemCreator());
		newItem.setTuneCount(Math.max(0, Math.min(sourceItem.getTuneCount(), newItem.getItemTemplate().getMaxTuneCount())));
		newItem.setEnchantLevel(sourceItem.getEnchantLevel() - 5);
		newItem.setEnchantBonus(sourceItem.getEnchantBonus());
		newItem.setAmplified(sourceItem.isAmplified() && newItem.getEnchantLevel() >= newItem.getMaxEnchantLevel());
		if (newItem.isAmplified() && newItem.getEnchantLevel() >= 20) {
			newItem.setBuffSkill(sourceItem.getBuffSkill());
		}
		if (sourceItem.hasFusionedItem()) {
			newItem.setFusionedItem(sourceItem.getFusionedItemTemplate(), sourceItem.getFusionedItemBonusStatsId(),
				sourceItem.getFusionedItemOptionalSockets());
		}
		if (sourceItem.hasManaStones()) {
			for (ManaStone manaStone : sourceItem.getItemStones())
				ItemSocketService.addManaStone(newItem, manaStone.getItemId(), false);
		}
		if (sourceItem.hasFusionStones()) {
			for (ManaStone manaStone : sourceItem.getFusionStones())
				ItemSocketService.addManaStone(newItem, manaStone.getItemId(), true);
		}
		if (sourceItem.getGodStone() != null)
			newItem.addGodStone(sourceItem.getGodStone().getItemId(), sourceItem.getGodStone().getActivatedCount());
		if (sourceItem.getTempering() > 0)
			newItem.setTempering(sourceItem.getTempering());
		if (sourceItem.isSoulBound())
			newItem.setSoulBound(true);
		if (sourceItem.getBonusStatsId() > 0) {
			int statBonusId = sourceItem.getBonusStatsId();
			if (!DataManager.ITEM_RANDOM_BONUSES.areBonusSetsEqual(StatBonusType.INVENTORY, sourceItem.getItemTemplate().getStatBonusSetId(),
				newItem.getItemTemplate().getStatBonusSetId())) {
				statBonusId = TuningAction.getRandomStatBonusIdFor(newItem);
			}
			newItem.setBonusStats(statBonusId, true);
		}
		newItem.setItemColor(sourceItem.getItemColor());
		player.getInventory().add(newItem);
	}

}
