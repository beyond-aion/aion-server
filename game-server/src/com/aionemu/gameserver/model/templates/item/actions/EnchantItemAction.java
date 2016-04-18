package com.aionemu.gameserver.model.templates.item.actions;

import java.util.Iterator;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.configs.main.CustomConfig;
import com.aionemu.gameserver.configs.main.EnchantsConfig;
import com.aionemu.gameserver.controllers.observer.StartMovingListener;
import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.World;

/**
 * @author Nemiroff, Wakizashi, vlog
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "EnchantItemAction")
public class EnchantItemAction extends AbstractItemAction {

	// Count of required supplements
	@XmlAttribute(name = "count")
	private int count;
	// Min level of enchantable item
	@XmlAttribute(name = "min_level")
	private Integer min_level;
	// Max level of enchantable item
	@XmlAttribute(name = "max_level")
	private Integer max_level;
	@XmlAttribute(name = "manastone_only")
	private boolean manastone_only;
	@XmlAttribute(name = "chance")
	private float chance;

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		if (isSupplementAction())
			return false;
		if (targetItem == null) { // no item selected.
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_COLOR_ERROR());
			return false;
		}
		if (parentItem == null) {
			return false;
		}
		if (parentItem.getItemTemplate().getItemGroup() == ItemGroup.ENCHANTMENT) {
			if (targetItem.getItemTemplate().getMaxEnchantLevel() == 0 && !targetItem.getItemTemplate().canExceedEnchant()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_IT_CAN_NOT_BE_GIVEN_OPTION(new DescriptionId(targetItem
					.getItemTemplate().getNameId()), parentItem.getItemTemplate().getNameId()));
				return false;
			} else if (!targetItem.isAmplified() && targetItem.getEnchantLevel() >= targetItem.getMaxEnchantLevel()) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_IT_CAN_NOT_BE_GIVEN_OPTION_MORE_TIME(new DescriptionId(targetItem
					.getItemTemplate().getNameId()), parentItem.getItemTemplate().getNameId()));
				return false;
			} else if (targetItem.getEnchantLevel() >= EnchantsConfig.MAX_AMPLIFICATION_LEVEL) {
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_GIVE_ITEM_OPTION_IT_CAN_NOT_BE_GIVEN_OPTION_MORE_TIME(new DescriptionId(targetItem
					.getItemTemplate().getNameId()), parentItem.getItemTemplate().getNameId()));
				return false;
			} else if (targetItem.isAmplified() && parentItem.getItemId() != 166020000) { // only omega enchantment stone
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_EXCEED_CANNOT_02(new DescriptionId(parentItem.getItemTemplate().getNameId())));
				return false;
			}
		}
		int msID = parentItem.getItemTemplate().getTemplateId() / 1000000;
		int tID = targetItem.getItemTemplate().getTemplateId() / 1000000;
		return (msID == 167 || msID == 166) && tID < 120;
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem) {
		act(player, parentItem, targetItem, null, 1);
	}

	// necessary overloading to not change AbstractItemAction
	public void act(final Player player, final Item parentItem, final Item targetItem, final Item supplementItem, final int targetWeapon) {

		final boolean stoneType = parentItem.getItemTemplate().getItemGroup() == ItemGroup.ENCHANTMENT;

		if (supplementItem != null && !checkSupplementLevel(player, supplementItem.getItemTemplate(), targetItem.getItemTemplate()))
			return;

		final StartMovingListener move = new StartMovingListener() {

			@Override
			public void moved() {
				super.moved();
				player.getObserveController().removeObserver(this);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(stoneType ? 1300457 : 1300464, new DescriptionId(targetItem.getNameId())));
				if (player.getMoveController().isInMove()) {
					player.getMoveController().abortMove(false);
					player.getController().cancelUseItem();
				}
			}

		};

		player.getObserveController().attach(move);

		// Current enchant level
		final int currentEnchant = targetItem.getEnchantLevel();
		final boolean isSuccess = isSuccess(player, parentItem, targetItem, supplementItem, targetWeapon);
		// Item template
		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), targetItem.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate()
				.getTemplateId(), stoneType ? 5000 : 2000, 0, 0, 1, 0, 0));

		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(move);

				if (player.getInventory().getItemByObjId(targetItem.getObjectId()) == null && !targetItem.isEquipped()) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300452));
					PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
						.getItemTemplate().getTemplateId(), 0, 2, 0));
					return;
				}

				// Enchantment stone
				if (stoneType)
					EnchantService.enchantItemAct(player, parentItem, targetItem, supplementItem, currentEnchant, isSuccess);
				// Manastone
				else
					EnchantService.socketManastoneAct(player, parentItem, targetItem, supplementItem, targetWeapon, isSuccess);

				PacketSendUtility.broadcastPacketAndReceive(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
					.getItemTemplate().getTemplateId(), 0, isSuccess ? 1 : 2, 0));
				if (CustomConfig.ENABLE_ENCHANT_ANNOUNCE) {
					if (stoneType && (targetItem.getEnchantLevel() == 15 || targetItem.getEnchantLevel() == 20) && isSuccess) {
						Iterator<Player> iter = World.getInstance().getPlayersIterator();
						while (iter.hasNext()) {
							Player player2 = iter.next();
							if (player2.getRace() == player.getRace()) {
								if (targetItem.getEnchantLevel() == 15) {
									PacketSendUtility.sendPacket(player2,
										SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_ITEM_SUCCEEDED_15(player.getName(), targetItem.getItemTemplate().getNameId()));
								} else if (targetItem.getEnchantLevel() == 20) {
									PacketSendUtility.sendPacket(player2,
										SM_SYSTEM_MESSAGE.STR_MSG_ENCHANT_ITEM_SUCCEEDED_20(player.getName(), targetItem.getItemTemplate().getNameId()));
								}
							}
						}
					}
				}
			}

		}, stoneType ? 5000 : 2000));
	}

	/**
	 * Check, if the item enchant will be successful
	 *
	 * @param player
	 * @param parentItem
	 *          the enchantment-/manastone to insert
	 * @param targetItem
	 *          the current item to enchant
	 * @param supplementItem
	 *          the item to increase the enchant chance (if exists)
	 * @param targetWeapon
	 *          the fused weapon (if exists)
	 * @param currentEnchant
	 *          current enchant level
	 * @return true if successful
	 */
	private boolean isSuccess(final Player player, final Item parentItem, final Item targetItem, final Item supplementItem, final int targetWeapon) {
		if (parentItem.getItemTemplate() != null) {
			// Item template
			ItemTemplate itemTemplate = parentItem.getItemTemplate();
			// Enchantment stone
			if (itemTemplate.getItemGroup() == ItemGroup.ENCHANTMENT) {
				return EnchantService.enchantItem(player, parentItem, targetItem, supplementItem);
			}
			// Manastone
			return EnchantService.socketManastone(player, parentItem, targetItem, supplementItem, targetWeapon);
		}
		return false;
	}

	public int getCount() {
		return count;
	}

	public int getMaxLevel() {
		return max_level != null ? max_level : 0;
	}

	public int getMinLevel() {
		return min_level != null ? min_level : 0;
	}

	public boolean isManastoneOnly() {
		return manastone_only;
	}

	public float getChance() {
		return chance;
	}

	boolean isSupplementAction() {
		return getMinLevel() > 0 || getMaxLevel() > 0 || getChance() > 0 || isManastoneOnly();
	}

	private boolean checkSupplementLevel(final Player player, final ItemTemplate supplementTemplate, final ItemTemplate targetItemTemplate) {
		// Is item manastone? True - check if player can use supplement
		if (supplementTemplate.getItemGroup() != ItemGroup.ENCHANTMENT) {
			// Check if max item level is ok for the enchant
			int minEnchantLevel = targetItemTemplate.getLevel();
			int maxEnchantLevel = targetItemTemplate.getLevel();

			EnchantItemAction action = supplementTemplate.getActions().getEnchantAction();
			if (action != null) {
				if (action.getMinLevel() != 0)
					minEnchantLevel = action.getMinLevel();
				if (action.getMaxLevel() != 0)
					maxEnchantLevel = action.getMaxLevel();
			}

			if (minEnchantLevel <= targetItemTemplate.getLevel() && maxEnchantLevel >= targetItemTemplate.getLevel())
				return true;

			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_ITEM_ENCHANT_ASSISTANT_NO_RIGHT_ITEM());
			return false;
		}
		return true;
	}

}
