package com.aionemu.gameserver.services.toypet;

import static com.aionemu.gameserver.model.items.ItemUseAnimation.*;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dao.PlayerPetsDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Pet;
import com.aionemu.gameserver.model.gameobjects.PetDopingAction;
import com.aionemu.gameserver.model.gameobjects.PetFeedAction;
import com.aionemu.gameserver.model.gameobjects.PetSpecialFunction;
import com.aionemu.gameserver.model.gameobjects.player.PetCommonData;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.team.common.legacy.LootRuleType;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.actions.AbstractItemAction;
import com.aionemu.gameserver.model.templates.item.actions.SkillUseAction;
import com.aionemu.gameserver.model.templates.pet.*;
import com.aionemu.gameserver.model.trade.TradeList;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FUNCTIONAL_PET;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.TradeService;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect.ForceType;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.utils.Util;
import com.aionemu.gameserver.utils.audit.AuditLogger;

/**
 * @author M@xx, IlBuono, xTz, Rolandas
 */
public class PetService {

	private static final Logger log = LoggerFactory.getLogger(PetService.class);

	public static PetService getInstance() {
		return SingletonHolder.instance;
	}

	private PetService() {
	}

	public void renamePet(Player player, int petObjectId, String name) {
		name = Util.convertName(name);
		PetCommonData petCommonData = player.getPetList().getPets().stream().filter(pet -> pet.getObjectId() == petObjectId).findFirst()
			.orElse(null);
		if (petCommonData == null)
			return;
		petCommonData.setName(name);
		PlayerPetsDAO.updatePetName(petCommonData);
		PacketSendUtility.broadcastPacket(player, SM_FUNCTIONAL_PET.renamed(petObjectId, petCommonData.getName()), true);
	}

	public void onPlayerLogin(Player player) {
		Collection<PetCommonData> playerPets = player.getPetList().getPets();
		if (playerPets.isEmpty())
			return;
		List<PetCommonData> pets = new ArrayList<>(playerPets);
		for (int page = 0; page * SM_FUNCTIONAL_PET.MAX_ENTRIES_PER_PAGE < pets.size(); page++) {
			int from = page * SM_FUNCTIONAL_PET.MAX_ENTRIES_PER_PAGE;
			int to = Math.min(from + SM_FUNCTIONAL_PET.MAX_ENTRIES_PER_PAGE, pets.size());
			PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.petList(pets.subList(from, to), page));
		}
	}

	public void removeObject(int objectId, int count, Player player) {
		Pet pet = player.getPet();
		Item item = player.getInventory().getItemByObjId(objectId);
		if (item == null || pet == null || pet.getCommonData().getFeedProgress() == null || count > item.getItemCount())
			return;

		pet.getCommonData().setCancelFeed(false);
		PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.feeding(PetFeedAction.START, item.getObjectId(), count, pet));
		PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.START_FEEDING, 0, player.getObjectId()));

		schedule(pet, player, item, count);
	}

	private void schedule(final Pet pet, final Player player, final Item item, final int count) {
		ThreadPoolManager.getInstance().schedule(() -> {
			if (!pet.getCommonData().getCancelFeed())
				checkFeeding(pet, player, item, count);
		}, 2500);
	}

	private void checkFeeding(Pet pet, Player player, Item item, int count) {
		PetCommonData commonData = pet.getCommonData();
		PetFeedProgress progress = commonData.getFeedProgress();

		if (!commonData.getCancelFeed()) {
			PetFunction func = pet.getObjectTemplate().getPetFunction(PetFunctionType.FOOD);
			PetFlavour flavour = DataManager.PET_FEED_DATA.getFlavourById(func.getId());
			FoodType foodType = flavour.getFoodType(item.getItemId());

			if (flavour.isLovedFood(foodType, item.getItemId()) && progress.getLovedFoodRemaining() == 0)
				foodType = null;

			if (foodType == null) {
				// non eatable item
				ItemPacketService.sendItemUnlockPacket(player, item);
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.feeding(PetFeedAction.REJECTED, item.getObjectId(), count, pet));
				PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
				PacketSendUtility.sendPacket(player,
					SM_SYSTEM_MESSAGE.STR_MSG_TOYPET_FEED_FOOD_NOT_LOVEFLAVOR(pet.getName(), item.getItemTemplate().getL10n()));
				return;
			}
			player.getInventory().decreaseItemCount(item, 1, ItemUpdateType.DEC_PET_FOOD);
			PetFeedResult reward = flavour.processFeedResult(progress, foodType, item.getItemTemplate().getLevel(), player.getCommonData().getLevel());

			if (progress.getHungryLevel() == PetHungryLevel.FULL && reward != null) {
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.feeding(PetFeedAction.EATEN, item.getObjectId(), 0, pet));
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.feeding(PetFeedAction.GAVE_ITEM, reward.getItem(), 0, pet));
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.feeding(PetFeedAction.FINISHED, 0, 0, pet));
				PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.feeding(PetFeedAction.REFEED, item.getObjectId(), 0, pet)); // 2151591961

				ItemService.addItem(player, reward.getItem(), 1);
				long delay = flavour.getCooldDown() * 60000;
				commonData.scheduleRefeed(delay);
				long refeedTime = System.currentTimeMillis() + delay;
				commonData.setRefeedTime(refeedTime);
				PlayerPetsDAO.setTime(pet.getObjectId(), refeedTime);
				progress.reset();
			} else {
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.feeding(PetFeedAction.EATEN, item.getObjectId(), --count, pet));
				if (count > 0)
					schedule(pet, player, item, count);
				else {
					PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.feeding(PetFeedAction.FINISHED, 0, 0, pet));
					PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.END_FEEDING, 0, player.getObjectId()));
				}
			}
		}
	}

	public void useDoping(Pet pet, PetDopingAction action, int itemId, int slot, int slot2) {
		Player player = pet.getMaster();
		if (pet.getCommonData().getDopingBag() == null) {
			PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.dopingRejected(action));
			return;
		}

		if (action == PetDopingAction.ADD || action == PetDopingAction.REMOVE) {
			if (!validateSetDopeItem(pet, itemId, slot)) {
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.dopingRejected(action));
				return;
			}
			pet.getCommonData().getDopingBag().setItem(itemId, slot);
			PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.doping(action, itemId, slot));
		} else if (action == PetDopingAction.SWITCH) {
			if (slot < 2 || slot2 < 2 || slot >= PetDopingBag.MAX_ITEMS || slot2 >= PetDopingBag.MAX_ITEMS) {
				AuditLogger.log(player, "tried to move pet buff items in slots " + slot + " and " + slot2);
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.dopingRejected(action));
				return;
			}
			pet.getCommonData().getDopingBag().switchItems(slot, slot2);
			PacketSendUtility.sendPacket(pet.getMaster(), SM_FUNCTIONAL_PET.doping(action, slot2, slot));
		} else if (action == PetDopingAction.USE) { // use item
			if (!player.isSpawned()) { // player is between maps, schedule re-check
				ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.doping(action, itemId, slot)), 5, TimeUnit.SECONDS);
				return;
			}

			int[] dopingItems = pet.getCommonData().getDopingBag().getItems();
			if (slot < 0 || slot >= dopingItems.length || dopingItems[slot] != itemId) {
				AuditLogger.log(player, "tried to use item " + itemId + " through pet buffing, but it's not in slot " + slot);
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.dopingRejected(action));
				return;
			}
			Item useItem = player.getInventory().getFirstItemByItemId(itemId);
			if (useItem == null || !useItem.getItemTemplate().isDopingPetUsable()) {
				PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.dopingRejected(action));
				return;
			}

			if (!isPetItemUseAllowed(player, useItem)) { // pet currently is not allowed to buff, schedule re-check
				ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.doping(action, itemId, slot)), 20, TimeUnit.SECONDS);
				return;
			}

			long now = System.currentTimeMillis();
			long reuseTime = player.getItemReuseTime(useItem.getItemTemplate().getUseLimits().getDelayId());
			if (reuseTime != 0 && reuseTime > now) { // player still has cooldown, schedule re-check
				ThreadPoolManager.getInstance().schedule(() -> PacketSendUtility.sendPacket(player, SM_FUNCTIONAL_PET.doping(action, itemId, slot)), reuseTime - now);
				return;
			}

			boolean used = false;
			for (AbstractItemAction itemAction : useItem.getItemTemplate().getActions().getItemActions()) {
				if (itemAction instanceof SkillUseAction) {
					PacketSendUtility.broadcastPacket(player,
						new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), player.getObjectId(), useItem.getObjectId(), useItem.getItemId(), 0, USE_SUCCESS, true),
						true);
					SkillEngine.getInstance().applyEffectDirectly(((SkillUseAction) itemAction).getSkillId(), ((SkillUseAction) itemAction).getLevel(), player,
						player, null, ForceType.DEFAULT);
					int useDelay = useItem.getItemTemplate().getUseLimits().getDelayTime();
					player.addItemCoolDown(useItem.getItemTemplate().getUseLimits().getDelayId(), now + useDelay, useDelay / 1000);
					player.getInventory().decreaseByItemId(itemId, 1);
					used = true;
				} else
					log.warn("Pet attempt to use not skill use item");
			}
			PacketSendUtility.sendPacket(player, used ? SM_FUNCTIONAL_PET.doping(action, itemId, slot) : SM_FUNCTIONAL_PET.dopingRejected(action));
		}
	}

	private boolean validateSetDopeItem(Pet pet, int itemId, int slot) {
		PetFunction petFunction = pet.getObjectTemplate().getPetFunction(PetFunctionType.DOPING);
		if (petFunction == null) {
			AuditLogger.log(pet.getMaster(), "tried to set buff item " + itemId + " but " + pet + " doesn't support buffing");
			return false;
		}
		if (itemId != 0) {
			ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
			if (itemTemplate == null || !itemTemplate.isDopingPetUsable()) {
				AuditLogger.log(pet.getMaster(), "tried to set item " + itemId + " in pet buff slot " + slot + " but it can't be used by pets");
				return false;
			}
		}
		PetDopingEntry dope = DataManager.PET_DOPING_DATA.getDopingTemplate(petFunction.getId());
		if (slot == 0 && !dope.isUseFood()) {
			AuditLogger.log(pet.getMaster(), "tried to set item " + itemId + " in pet buff food slot but " + pet + " doesn't support buffing with food");
			return false;
		}
		if (slot == 1 && !dope.isUseDrink()) {
			AuditLogger.log(pet.getMaster(), "tried to set item " + itemId + " in pet buff drink slot but " + pet + " doesn't support buffing with drinks");
			return false;
		}
		if (slot > 1 && slot - 1 > dope.getScrollsUsed()) {
			AuditLogger.log(pet.getMaster(), "tried to set item " + itemId + " in pet buff scroll slot " + (slot - 1) + " but " + pet + " only supports "
				+ dope.getScrollsUsed() + " scrolls");
			return false;
		}
		return true;
	}

	private boolean isPetItemUseAllowed(Player player, Item item) {
		return !item.getItemTemplate().hasAreaRestriction() || player.isInsideItemUseZone(item.getItemTemplate().getUseArea());
	}

	public void activateLoot(Pet pet, boolean activate) {
		if (activate) {
			if (!pet.getObjectTemplate().containsFunction(PetFunctionType.LOOT)) {
				AuditLogger.log(pet.getMaster(), "tried to enable auto-loot on non-looting " + pet);
				return;
			}
			var team = pet.getMaster().getCurrentTeam();
			if (team != null && team.getLootGroupRules().getLootRule() == LootRuleType.FREEFORALL) {
				PacketSendUtility.sendPacket(pet.getMaster(), SM_SYSTEM_MESSAGE.STR_MSG_LOOTING_PET_MESSAGE03());
				return;
			}
			PacketSendUtility.sendPacket(pet.getMaster(), SM_SYSTEM_MESSAGE.STR_MSG_LOOTING_PET_MESSAGE01());
		}
		pet.getCommonData().setIsLooting(activate);
		PacketSendUtility.sendPacket(pet.getMaster(), SM_FUNCTIONAL_PET.specialFunction(PetSpecialFunction.AUTOLOOT, activate));
	}

	public void activateAutoSell(Pet pet, boolean activate) {
		if (activate && !pet.getObjectTemplate().containsFunction(PetFunctionType.MERCHANT)) {
			AuditLogger.log(pet.getMaster(), "tried to enable auto-sell on non-selling " + pet);
			return;
		}
		pet.getCommonData().setIsSelling(activate);
		PacketSendUtility.sendPacket(pet.getMaster(), SM_FUNCTIONAL_PET.specialFunction(PetSpecialFunction.AUTOSELL, activate));
	}

	public void sell(Pet pet, List<Item> items) {
		if (pet == null || !pet.getCommonData().isSelling())
			return;
		PetFunction pf = pet.getObjectTemplate().getPetFunction(PetFunctionType.MERCHANT);
		if (pf != null) {
			TradeList tradeList = new TradeList(pet.getObjectId());
			for (Item item : items)
				tradeList.addItem(item.getObjectId(), item.getItemCount());
			if (tradeList.size() > 0) {
				TradeService.performSellToShop(pet.getMaster(), tradeList, null, pf.getRatePrice());
				PacketSendUtility.sendPacket(pet.getMaster(), SM_SYSTEM_MESSAGE.STR_MSG_MERCHANT_PET_GET_SELL_ITEM(pet.getName()));
			}
		}
	}

	private static class SingletonHolder {

		protected static final PetService instance = new PetService();
	}

}
