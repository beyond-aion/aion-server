package com.aionemu.gameserver.model.templates.item.actions;

import java.util.*;
import java.util.stream.Collectors;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Chance;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.*;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FIRST_SHOW_DECOMPOSABLE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author oslo(a00441234)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DecomposeAction")
public class DecomposeAction extends AbstractItemAction {

	private static final Logger log = LoggerFactory.getLogger(DecomposeAction.class);
	public static final int USAGE_DELAY = 3000;
	private static Map<Race, int[]> chunkEarth = new HashMap<>();
	private static Map<Race, int[]> chunkSand = new HashMap<>();
	private static Map<Race, int[]> premiumOphidanRecipe = new HashMap<>();

	private static int[] chunkRock = { 152000104, 152000107, 152000113, 152000204, 152000207, 152000214, 152000307, 152000309, 152000311, 152000313,
		152000315, 152000317, 152000320, 152000322, 152000324 };

	private static int[] chunkGemstone = { 152000112, 152000116, 152000212, 152000213, 152000217, 152000326, 152000327, 152000328 };

	private static int[] scrolls = { 164000073, 164000134, 164000076, 164000079, 164000122, 164000131, 164000118 };

	private static int[] potion = { 162000045, 162000079, 162000016, 162000021, 162000027, 162000023 };

	private static int[] lesser_potions = { 162000003, 162000008, 162000042, 162000022, 162000013, 162000018, 162000047 };

	private static int[] potion_50 = { 162000075, 162000076, 162000077, 162000078, 162000079, 162000080, 162000081 };

	private static int[] illusion_godstones = { 168000161, 168000162, 168000163, 168000164, 168000165, 168000166, 168000167, 168000168, 168000169,
		168000170, 168000171, 168000172, 168000173, 168000174, 168000175, 168000176, 168000177 };

	static {
		chunkEarth.put(Race.ASMODIANS,
			new int[] { 152000051, 152000052, 152000053, 152000054, 152000055, 152000056, 152000057, 152000058, 152000059, 152000061, 152000062, 152000063,
				152000101, 152000102, 152000104, 152000107, 152000113, 152000201, 152000202, 152000204, 152000207, 152000214, 152000451, 152000453, 152000455,
				152000457, 152000459, 152000461, 152000463, 152000465, 152000468, 152000470, 152000551, 152000552, 152000553, 152000554, 152000556, 152000651,
				152000652, 152000653, 152000654, 152000656, 152000751, 152000752, 152000753, 152000754, 152000755, 152000756, 152000757, 152000758, 152000759,
				152000760, 152000762, 152000763, 152000851, 152000852, 152000853, 152000854, 152000855, 152000856, 152000857, 152000858, 152000860, 152000861,
				152001051, 152001052, 152001053, 152001055, 152001056 });
		chunkEarth.put(Race.ELYOS,
			new int[] { 152000001, 152000002, 152000003, 152000004, 152000005, 152000006, 152000007, 152000008, 152000009, 152000010, 152000011, 152000012,
				152000101, 152000102, 152000104, 152000107, 152000113, 152000201, 152000202, 152000204, 152000207, 152000214, 152000401, 152000403, 152000405,
				152000407, 152000409, 152000411, 152000413, 152000415, 152000417, 152000419, 152000501, 152000502, 152000503, 152000504, 152000505, 152000601,
				152000602, 152000603, 152000604, 152000605, 152000701, 152000702, 152000703, 152000704, 152000705, 152000706, 152000707, 152000708, 152000709,
				152000710, 152000711, 152000712, 152000801, 152000802, 152000803, 152000804, 152000805, 152000806, 152000807, 152000808, 152000809, 152000810,
				152001001, 152001002, 152001003, 152001004, 152001005 });

		chunkSand.put(Race.ASMODIANS,
			new int[] { 152000452, 152000454, 152000301, 152000302, 152000303, 152000456, 152000458, 152000103, 152000203, 152000304, 152000305, 152000306,
				152000460, 152000462, 152000105, 152000205, 152000307, 152000309, 152000311, 152000464, 152000466, 152000108, 152000208, 152000313, 152000315,
				152000317, 152000469, 152000471, 152000114, 152000215, 152000320, 152000322, 152000324 });
		chunkSand.put(Race.ELYOS,
			new int[] { 152000402, 152000404, 152000301, 152000302, 152000303, 152000406, 152000408, 152000103, 152000203, 152000304, 152000305, 152000306,
				152000410, 152000412, 152000105, 152000205, 152000307, 152000309, 152000311, 152000414, 152000416, 152000108, 152000208, 152000313, 152000315,
				152000317, 152000418, 152000420, 152000114, 152000215, 152000320, 152000322, 152000324 });

		premiumOphidanRecipe.put(Race.ASMODIANS,
			new int[] { 152230698, 152230699, 152230700, 152230701, 152230702, 152230703, 152230704, 152230759, 152230760, 152230761, 152230762, 152230763,
				152230764, 152230839, 152230840, 152230841, 152230842, 152230843, 152230844, 152230845, 152231021, 152231022, 152231023, 152231107, 152231108,
				152231253, 152231254, 152231255, 152231256, 152231257, 152231258, 152231313, 152231314, 152231315, 152231316, 152231317, 152231318, 152231385,
				152231386, 152231387, 152231388, 152231389, 152231390, 152231403, 152231404, 152231405, 152231406, 152231407, 152231408, 152231421, 152231422,
				152231423, 152231424, 152231425, 152231426, 152231439, 152231440, 152231441, 152231442, 152231443, 152231444, 152231566 });
		premiumOphidanRecipe.put(Race.ELYOS,
			new int[] { 152220709, 152220710, 152220711, 152220712, 152220713, 152220714, 152220715, 152220770, 152220771, 152220772, 152220773, 152220774,
				152220775, 152220850, 152220851, 152220852, 152220853, 152220854, 152220855, 152220856, 152221032, 152221033, 152221034, 152221118, 152221119,
				152221264, 152221265, 152221266, 152221267, 152221268, 152221269, 152221324, 152221325, 152221326, 152221327, 152221328, 152221329, 152221396,
				152221397, 152221398, 152221399, 152221400, 152221401, 152221414, 152221415, 152221416, 152221417, 152221418, 152221419, 152221432, 152221433,
				152221434, 152221435, 152221436, 152221437, 152221450, 152221451, 152221452, 152221453, 152221454, 152221455, 152221576 });
	}

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		if (player.isDead() || !player.isSpawned())
			return false;
		List<ExtractedItemsCollection> itemsCollections = null;
		itemsCollections = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(parentItem.getItemId());
		if (itemsCollections == null || itemsCollections.isEmpty()) {
			if (DataManager.DECOMPOSABLE_ITEMS_DATA.getSelectableItems(parentItem.getItemId()) != null) // selectable decomposable
				return true;
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(parentItem.getL10n()));
			return false;
		}
		if (player.getInventory().isFull() || player.getInventory().isFullSpecialCube() && containsSpecialCubeItems(itemsCollections, player)) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_INVENTORY_IS_FULL());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem, Object... params) {
		player.getController().cancelUseItem();
		Collection<ResultedItem> selectable = DataManager.DECOMPOSABLE_ITEMS_DATA.getSelectableItems(parentItem.getItemId());
		if (selectable != null) {
			selectable.removeIf(item -> !item.isObtainableFor(player));
			PacketSendUtility.sendPacket(player, new SM_FIRST_SHOW_DECOMPOSABLE(parentItem.getObjectId(), selectable));
			return;
		}
		List<ExtractedItemsCollection> itemsCollections = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(parentItem.getItemId());
		Collection<ExtractedItemsCollection> levelSuitableItems = filterItemsByLevel(player, itemsCollections);
		final ExtractedItemsCollection selectedCollection = Chance.selectElement(levelSuitableItems);
		if (selectedCollection.getRandomItems().isEmpty() && selectedCollection.getItems().stream().noneMatch(i -> i.isObtainableFor(player))) {
			log.warn(
				"Empty decomposable " + parentItem.getItemId() + " for " + player + ", class: " + player.getPlayerClass() + ", level: " + player.getLevel());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_FAILED(parentItem.getL10n()));
			return;
		}

		PacketSendUtility.broadcastPacket(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), USAGE_DELAY, 0, 0), true);

		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_CANCELED(parentItem.getL10n()));
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemTemplate().getTemplateId(), 0, 2, 0), true);
				player.getObserveController().removeObserver(this);
			}

		};

		player.getObserveController().attach(observer);
		player.getController().addTask(TaskId.ITEM_USE, ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				player.getObserveController().removeObserver(observer);
				boolean validAction = postValidate(player, parentItem);
				if (validAction) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_SUCCEED(parentItem.getL10n()));
					for (ResultedItem resultItem : selectedCollection.getItems()) {
						if (resultItem.isObtainableFor(player)) {
							int count = Rnd.get(resultItem.getMinCount(), resultItem.getMaxCount());
							ItemService.addItem(player, resultItem.getItemId(), count, true,
								new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_ITEM_COLLECT));
						}
					}
					for (RandomItem randomItem : selectedCollection.getRandomItems()) {
						RandomType randomType = randomItem.getType();
						if (randomType != null) {
							int randomId = 0;
							int i = 0;
							int itemLvl = parentItem.getItemTemplate().getLevel();
							switch (randomItem.getType()) {
								case ENCHANTMENT:
									do {
										randomId = 166000191 + Math.round(itemLvl / 100f) + Rnd.nextInt(4);
										i++;
										if (i > 50) {
											randomId = 0;
											break;
										}
									} while (!isValidItemId(randomId));
									break;
								case MANASTONE:
								case MANASTONE_COMMON_GRADE_10:
								case MANASTONE_COMMON_GRADE_20:
								case MANASTONE_COMMON_GRADE_30:
								case MANASTONE_COMMON_GRADE_40:
								case MANASTONE_COMMON_GRADE_50:
								case MANASTONE_COMMON_GRADE_60:
								case MANASTONE_COMMON_GRADE_70:
								case MANASTONE_RARE_GRADE_10:
								case MANASTONE_RARE_GRADE_20:
								case MANASTONE_RARE_GRADE_30:
								case MANASTONE_RARE_GRADE_40:
								case MANASTONE_RARE_GRADE_50:
								case MANASTONE_RARE_GRADE_60:
								case MANASTONE_RARE_GRADE_70:
								case MANASTONE_LEGEND_GRADE_10:
								case MANASTONE_LEGEND_GRADE_20:
								case MANASTONE_LEGEND_GRADE_30:
								case MANASTONE_LEGEND_GRADE_40:
								case MANASTONE_LEGEND_GRADE_50:
								case MANASTONE_LEGEND_GRADE_60:
								case MANASTONE_LEGEND_GRADE_70:
									if (randomType == RandomType.MANASTONE) // stone level near or equal to item level (if 1, near player level)
										itemLvl = itemLvl % 10 == 0 ? itemLvl : ((int) Math.ceil((itemLvl == 1 ? player.getLevel() : itemLvl) / 10f) * 10);
									else
										itemLvl = randomType.getLevel();
									List<ItemTemplate> stones = DataManager.ITEM_DATA.getManastones(itemLvl);
									if (stones == null) {
										log.warn("No lv" + itemLvl + " manastones found for decomposable random type " + randomItem.getType());
										break;
									}
									if (randomType != RandomType.MANASTONE) {
										final ItemQuality itemQuality;
										if (randomType.name().contains("RARE"))
											itemQuality = ItemQuality.RARE;
										else if (randomType.name().contains("LEGEND"))
											itemQuality = ItemQuality.LEGEND;
										else
											itemQuality = ItemQuality.COMMON;
										List<ItemTemplate> selectedStones = stones.stream()
											.filter(t -> t.getItemQuality() == itemQuality && !t.getName().contains(" MP ")).collect(Collectors.toList());
										randomId = Rnd.get(selectedStones).getTemplateId();
									} else {
										List<ItemTemplate> selectedStones = stones.stream()
											.filter(t -> t.getItemQuality() != ItemQuality.LEGEND && !t.getName().contains(" MP ")).collect(Collectors.toList());
										randomId = Rnd.get(selectedStones).getTemplateId();
									}
									break;
								case SPECIAL_MANASTONE_RARE_GRADE:
								case SPECIAL_MANASTONE_LEGEND_GRADE:
								case SPECIAL_MANASTONE_UNIQUE_GRADE:
								case SPECIAL_MANASTONE_EPIC_GRADE:
									List<ItemTemplate> ancientStones = DataManager.ITEM_DATA.getAncientManastones(randomType.getLevel());
									if (ancientStones == null) {
										log.warn("No ancient manastones found for decomposable random type " + randomItem.getType());
										break;
									}
									final ItemQuality itemQuality;
									if (randomType.name().contains("RARE"))
										itemQuality = ItemQuality.RARE;
									else if (randomType.name().contains("LEGEND"))
										itemQuality = ItemQuality.LEGEND;
									else if (randomType.name().contains("UNIQUE"))
										itemQuality = ItemQuality.UNIQUE;
									else if (randomType.name().contains("EPIC"))
										itemQuality = ItemQuality.EPIC;
									else
										itemQuality = ItemQuality.COMMON;
									List<ItemTemplate> selectedStones = ancientStones.stream()
										.filter(t -> t.getItemQuality() == itemQuality && !t.getName().contains(" MP ")).collect(Collectors.toList());
									randomId = Rnd.get(selectedStones).getTemplateId();
									break;
								case CHUNK_EARTH:
									int[] earth = chunkEarth.get(player.getRace());
									randomId = Rnd.get(earth);
									break;
								case CHUNK_SAND:
									int[] sand = chunkSand.get(player.getRace());
									randomId = Rnd.get(sand);
									break;
								case PREMIUM_OPHIDAN_RECIPE:
									int[] recipe = premiumOphidanRecipe.get(player.getRace());
									randomId = Rnd.get(recipe);
									break;
								case CHUNK_ROCK:
									randomId = Rnd.get(chunkRock);
									break;
								case CHUNK_GEMSTONE:
									randomId = Rnd.get(chunkGemstone);
									break;
								case SCROLLS:
									randomId = Rnd.get(scrolls);
									break;
								case POTION:
									randomId = Rnd.get(potion);
									break;
								case LESSER_POTIONS:
									randomId = Rnd.get(lesser_potions);
									break;
								case POTION_50:
									randomId = Rnd.get(potion_50);
									break;
								case ILLUSION_GODSTONE:
									randomId = Rnd.get(illusion_godstones);
									break;
								case ANCIENTITEMS:
									do {
										randomId = Rnd.get(186000051, 186000066);
										i++;
										if (i > 50) {
											randomId = 0;
											break;
										}
									} while (!isValidItemId(randomId));
									break;
								case ANCIENT_CROWN:
									do {
										randomId = Rnd.get(186000051, 186000054);
										i++;
										if (i > 50) {
											randomId = 0;
											break;
										}
									} while (!isValidItemId(randomId));
									break;
								case ANCIENT_GOBLET:
									do {
										randomId = Rnd.get(186000055, 186000058);
										i++;
										if (i > 50) {
											randomId = 0;
											break;
										}
									} while (!isValidItemId(randomId));
									break;
								case ANCIENT_SEAL:
									do {
										randomId = Rnd.get(186000059, 186000062);
										i++;
										if (i > 50) {
											randomId = 0;
											break;
										}
									} while (!isValidItemId(randomId));
									break;
								case ANCIENT_ICON:
									do {
										randomId = Rnd.get(186000063, 186000066);
										i++;
										if (i > 50) {
											randomId = 0;
											break;
										}
									} while (!isValidItemId(randomId));
									break;
							}
							if (randomId != 0) {
								int count = Rnd.get(randomItem.getMinCount(), randomItem.getMaxCount());
								ItemService.addItem(player, randomId, count, true,
									new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_ITEM_COLLECT));
							}
						}
					}
				}
				PacketSendUtility.broadcastPacket(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, validAction ? 1 : 2, 0), true);
			}

			boolean postValidate(Player player, Item parentItem) {
				if (!canAct(player, parentItem, targetItem)) {
					return false;
				}
				if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_NO_TARGET_ITEM());
					return false;
				}
				return true;
			}

		}, USAGE_DELAY));
	}

	/**
	 * Add to result collection only items wich suits player's level
	 */
	private Collection<ExtractedItemsCollection> filterItemsByLevel(Player player, List<ExtractedItemsCollection> itemsCollections) {
		if (itemsCollections == null) {
			return null;
		}
		int playerLevel = player.getLevel();
		Collection<ExtractedItemsCollection> result = new ArrayList<>();
		for (ExtractedItemsCollection collection : itemsCollections) {
			if (collection.getMinLevel() > playerLevel || collection.getMaxLevel() < playerLevel) {
				continue;
			}
			result.add(collection);
		}
		return result;
	}

	private boolean containsSpecialCubeItems(List<ExtractedItemsCollection> itemGroups, Player player) {
		for (ExtractedItemsCollection items : itemGroups) {
			if (items.getMinLevel() > player.getLevel() || items.getMaxLevel() < player.getLevel())
				continue;
			for (ResultedItem item : items.getItems()) {
				if (item.isObtainableFor(player)) {
					ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(item.getItemId());
					if (template == null)
						log.error("Detected invalid item id during decompose action " + item.getItemId());
					else if (template.getExtraInventoryId() > 0)
						return true;
				}
			}
		}
		return false;
	}

	public static void validateRandomItemIds() {
		for (int[] itemIds : chunkEarth.values())
			validateItemIds(itemIds);
		for (int[] itemIds : chunkSand.values())
			validateItemIds(itemIds);
		validateItemIds(chunkRock, chunkGemstone, scrolls, potion, lesser_potions, potion_50, illusion_godstones);
	}

	private static void validateItemIds(int[]... itemIds) {
		for (int[] ids : itemIds) {
			for (int itemId : ids)
				if (!isValidItemId(itemId))
					throw new IllegalArgumentException("Decomposable random reward item ID is invalid: " + itemId);
		}
	}

	private static boolean isValidItemId(int itemId) {
		return DataManager.ITEM_DATA.getItemTemplate(itemId) != null;
	}
}
