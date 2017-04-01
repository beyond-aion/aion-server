package com.aionemu.gameserver.services.item;

import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.RandomStats;
import com.aionemu.gameserver.model.templates.item.purification.ItemPurificationTemplate;
import com.aionemu.gameserver.model.templates.item.purification.PurificationResultItem;
import com.aionemu.gameserver.model.templates.item.purification.SubMaterialItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.abyss.AbyssPointsService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author Ranastic
 */
public class ItemPurificationService {

	private static final Logger log = LoggerFactory.getLogger(ItemPurificationService.class);

	public static boolean checkItemUpgrade(Player player, Item baseItem, int resultItemId) {
		ItemPurificationTemplate itemPurificationTemplate = DataManager.ITEM_PURIFICATION_DATA.getItemPurificationTemplate(baseItem.getItemId());
		if (itemPurificationTemplate == null) {
			log.warn(resultItemId + " item's purification template is null");
			return false;
		}

		Map<Integer, PurificationResultItem> resultItemMap = DataManager.ITEM_PURIFICATION_DATA.getResultItemMap(baseItem.getItemId());

		if (!resultItemMap.containsKey(resultItemId)) {
			AuditLogger.info(player, resultItemId + " item's baseItem and resultItem is not matched (possible client modify)");
			return false;
		}

		PurificationResultItem resultItem = resultItemMap.get(resultItemId);

		if (baseItem.getEnchantLevel() < resultItem.getCheck_enchant_count()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT(new DescriptionId(baseItem.getNameId())));
			return false;
		}

		for (SubMaterialItem sub : resultItem.getUpgrade_materials().getSubMaterialItem()) {
			if (player.getInventory().getItemCountByItemId(sub.getId()) < sub.getCount()) {
				// sub Metarial is not enough
				return false;
			}
		}

		if (resultItem.getNeed_abyss_point() != null) {
			if (player.getAbyssRank().getAp() < resultItem.getNeed_abyss_point().getCount()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NEED_AP());
				return false;
			}
		}

		if (resultItem.getNeed_kinah() != null) {
			if (player.getInventory().getKinah() < resultItem.getNeed_kinah().getCount()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_REGISTER_ITEM_MSG_UPGRADE_CANNOT_NEED_QINA());
				return false;
			}
		}
		int nameId = DataManager.ITEM_DATA.getItemTemplate(resultItemId).getNameId();
		PacketSendUtility.sendPacket(player,
			SM_SYSTEM_MESSAGE.STR_ITEM_UPGRADE_MSG_UPGRADE_SUCCESS(new DescriptionId(baseItem.getNameId()), new DescriptionId(nameId)));
		return true;
	}

	public static boolean decreaseMaterial(Player player, Item baseItem, int resultItemId) {
		Map<Integer, PurificationResultItem> resultItemMap = DataManager.ITEM_PURIFICATION_DATA.getResultItemMap(baseItem.getItemId());

		PurificationResultItem resultItem = resultItemMap.get(resultItemId);

		for (SubMaterialItem item : resultItem.getUpgrade_materials().getSubMaterialItem()) {
			if (!player.getInventory().decreaseByItemId(item.getId(), item.getCount())) {
				AuditLogger.info(player, "try item upgrade without sub material");
				return false;
			}
		}

		if (resultItem.getNeed_abyss_point() != null)
			AbyssPointsService.setAp(player, -resultItem.getNeed_abyss_point().getCount());

		if (resultItem.getNeed_kinah() != null)
			player.getInventory().decreaseKinah(-resultItem.getNeed_kinah().getCount());

		player.getInventory().decreaseByObjectId(baseItem.getObjectId(), 1);

		return true;
	}

	public static void upgradeItem(Player player, Item sourceItem, int targetItemId) {
		Item newItem = ItemFactory.newItem(targetItemId, 1);
		newItem.setOptionalSocket(sourceItem.getOptionalSocket());
		newItem.setItemCreator(sourceItem.getItemCreator());
		newItem.setEnchantLevel(sourceItem.getEnchantLevel() - 5);
		newItem.setEnchantBonus(sourceItem.getEnchantBonus());
		newItem.setAmplified(sourceItem.isAmplified() && newItem.getEnchantLevel() >= newItem.getMaxEnchantLevel());
		if (newItem.isAmplified() && newItem.getEnchantLevel() >= 20) {
			newItem.setBuffSkill(sourceItem.getBuffSkill());
		}
		if (sourceItem.hasFusionedItem()) {
			newItem.setFusionedItem(sourceItem.getFusionedItemTemplate());
			newItem.setOptionalFusionSocket(sourceItem.getOptionalFusionSocket());
		}
		if (sourceItem.hasManaStones()) {
			for (ManaStone manaStone : sourceItem.getItemStones())
				ItemSocketService.addManaStone(newItem, manaStone.getItemId());
		}
		if (sourceItem.hasFusionStones()) {
			for (ManaStone manaStone : sourceItem.getFusionStones())
				ItemSocketService.addFusionStone(newItem, manaStone.getItemId());
		}
		if (sourceItem.getGodStone() != null)
			newItem.addGodStone(sourceItem.getGodStone().getItemId(), sourceItem.getGodStone().getActivatedCount());
		if (sourceItem.getTempering() > 0)
			newItem.setTempering(sourceItem.getTempering());
		if (sourceItem.isSoulBound())
			newItem.setSoulBound(true);
		if (sourceItem.getItemTemplate().getRandomBonusId() != 0 && newItem.getItemTemplate().getRandomBonusId() != 0) {
			newItem.setBonusNumber(sourceItem.getBonusNumber());
			newItem.setRandomStats(new RandomStats(newItem.getItemTemplate().getRandomBonusId(), newItem.getBonusNumber()));
			newItem.setTuneCount(1);
		}
		newItem.setItemColor(sourceItem.getItemColor());
		newItem.setRndBonus();
		player.getInventory().add(newItem);
	}

}
