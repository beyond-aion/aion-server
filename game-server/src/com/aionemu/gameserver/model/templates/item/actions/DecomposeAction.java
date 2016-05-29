package com.aionemu.gameserver.model.templates.item.actions;

import static ch.lambdaj.Lambda.having;
import static ch.lambdaj.Lambda.on;
import static ch.lambdaj.Lambda.select;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.not;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ItemUseObserver;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.model.templates.item.ExtractedItemsCollection;
import com.aionemu.gameserver.model.templates.item.ItemQuality;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.RandomItem;
import com.aionemu.gameserver.model.templates.item.RandomType;
import com.aionemu.gameserver.model.templates.item.ResultedItem;
import com.aionemu.gameserver.network.aion.serverpackets.SM_FIRST_SHOW_DECOMPOSABLE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemAddType;
import com.aionemu.gameserver.services.item.ItemPacketService.ItemUpdateType;
import com.aionemu.gameserver.services.item.ItemService;
import com.aionemu.gameserver.services.item.ItemService.ItemUpdatePredicate;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import javolution.util.FastTable;

/**
 * @author oslo(a00441234)
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "DecomposeAction")
public class DecomposeAction extends AbstractItemAction {

	private static final Logger log = LoggerFactory.getLogger(DecomposeAction.class);
	public static final int USAGE_DELAY = 3000;
	private static Map<Integer, List<ItemTemplate>> specialManastones;
	private static Map<Integer, List<ItemTemplate>> manastones;
	private static Map<Race, int[]> chunkEarth = new HashMap<Race, int[]>();

	static {
		chunkEarth.put(Race.ASMODIANS, new int[] { 152000051, 152000052, 152000053, 152000054, 152000055, 152000056, 152000057, 152000058, 152000059,
			152000061, 152000062, 152000063, 152000101, 152000102, 152000104, 152000107, 152000113, 152000201, 152000202, 152000204, 152000207, 152000214,
			152000451, 152000453, 152000455, 152000457, 152000459, 152000461, 152000463, 152000465, 152000468, 152000470, 152000551, 152000552, 152000553,
			152000554, 152000556, 152000651, 152000652, 152000653, 152000654, 152000656, 152000751, 152000752, 152000753, 152000754, 152000755, 152000756,
			152000757, 152000758, 152000759, 152000760, 152000762, 152000763, 152000851, 152000852, 152000853, 152000854, 152000855, 152000856, 152000857,
			152000858, 152000860, 152000861, 152001051, 152001052, 152001053, 152001055, 152001056 });

		chunkEarth.put(Race.ELYOS, new int[] { 152000001, 152000002, 152000003, 152000004, 152000005, 152000006, 152000007, 152000008, 152000009,
			152000010, 152000011, 152000012, 152000101, 152000102, 152000104, 152000107, 152000113, 152000201, 152000202, 152000204, 152000207, 152000214,
			152000401, 152000403, 152000405, 152000407, 152000409, 152000411, 152000413, 152000415, 152000417, 152000419, 152000501, 152000502, 152000503,
			152000504, 152000505, 152000601, 152000602, 152000603, 152000604, 152000605, 152000701, 152000702, 152000703, 152000704, 152000705, 152000706,
			152000707, 152000708, 152000709, 152000710, 152000711, 152000712, 152000801, 152000802, 152000803, 152000804, 152000805, 152000806, 152000807,
			152000808, 152000809, 152000810, 152001001, 152001002, 152001003, 152001004, 152001005 });
	}

	private static Map<Race, int[]> chunkSand = new HashMap<Race, int[]>();

	static {

		chunkSand.put(Race.ASMODIANS, new int[] { 152000452, 152000454, 152000301, 152000302, 152000303, 152000456, 152000458, 152000103, 152000203,
			152000304, 152000305, 152000306, 152000460, 152000462, 152000105, 152000205, 152000307, 152000309, 152000311, 152000464, 152000466, 152000108,
			152000208, 152000313, 152000315, 152000317, 152000469, 152000471, 152000114, 152000215, 152000320, 152000322, 152000324 });

		chunkSand.put(Race.ELYOS, new int[] { 152000402, 152000404, 152000301, 152000302, 152000303, 152000406, 152000408, 152000103, 152000203,
			152000304, 152000305, 152000306, 152000410, 152000412, 152000105, 152000205, 152000307, 152000309, 152000311, 152000414, 152000416, 152000108,
			152000208, 152000313, 152000315, 152000317, 152000418, 152000420, 152000114, 152000215, 152000320, 152000322, 152000324 });
	}

	private static int[] chunkRock = { 152000104, 152000107, 152000113, 152000204, 152000207, 152000214, 152000307, 152000309, 152000311, 152000313,
		152000315, 152000317, 152000320, 152000322, 152000324 };

	private static int[] chunkGemstone = { 152000112, 152000116, 152000121, 152000212, 152000213, 152000217, 152000326, 152000327, 152000328 };

	private static int[] scrolls = { 164000073, 164000134, 164000076, 164000079, 164000122, 164000131, 164000118 };

	private static int[] potion = { 162000045, 162000079, 162000016, 162000021, 162000027, 162000023 };

	private static int[] lesser_potions = { 162000003, 162000008, 162000042, 162000022, 162000013, 162000018, 162000047 };

	private static int[] potion_50 = { 162000075, 162000076, 162000077, 162000078, 162000079, 162000080, 162000081 };
	
	private static int[] illusion_godstones = { 168000229, 168000230, 168000231, 168000232, 168000233, 168000234, 168000235, 168000236, 168000237,
		168000238, 168000239, 168000240, 168000241, 168000242, 168000243, 168000244, 16800245 };

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		List<ExtractedItemsCollection> itemsCollections = null;
		itemsCollections = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(parentItem.getItemId());
		if (itemsCollections == null || itemsCollections.isEmpty()) {
			if (DataManager.DECOMPOSABLE_ITEMS_DATA.getSelectableItems(parentItem.getItemId()) != null) // selectable decomposable
				return true;
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(parentItem.getNameId()));
			return false;
		}
		if (player.getInventory().isFull()) {
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_INVENTORY_IS_FULL());
			return false;
		}
		return true;
	}

	@Override
	public void act(final Player player, final Item parentItem, final Item targetItem) {
		player.getController().cancelUseItem();
		Collection<ResultedItem> selectable = DataManager.DECOMPOSABLE_ITEMS_DATA.getSelectableItems(parentItem.getItemId());
		if (selectable != null && !selectable.isEmpty()) {
			Iterator<ResultedItem> iter = selectable.iterator();
			while (iter.hasNext()) {
				ResultedItem item = iter.next();
				if (!item.isObtainableFor(player))
					iter.remove();
			}
			PacketSendUtility.sendPacket(player, new SM_FIRST_SHOW_DECOMPOSABLE(parentItem.getObjectId(), selectable));
			return;
		}
		List<ExtractedItemsCollection> itemsCollections = DataManager.DECOMPOSABLE_ITEMS_DATA.getInfoByItemId(parentItem.getItemId());
		Collection<ExtractedItemsCollection> levelSuitableItems = filterItemsByLevel(player, itemsCollections);
		final ExtractedItemsCollection selectedCollection = selectItemByChance(levelSuitableItems);
		if (selectedCollection.getItems().isEmpty() && selectedCollection.getRandomItems().isEmpty()) {
			log.warn("Empty decomposable " + parentItem.getItemId() + " for " + player + ", class: " + player.getPlayerClass() + ", level: " + player.getLevel());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_IT_CAN_NOT_BE_DECOMPOSED(parentItem.getNameId()));
			return;
		}

		PacketSendUtility.broadcastPacketAndReceive(player,
			new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), USAGE_DELAY, 0, 0));

		final ItemUseObserver observer = new ItemUseObserver() {

			@Override
			public void abort() {
				player.getController().cancelTask(TaskId.ITEM_USE);
				player.removeItemCoolDown(parentItem.getItemTemplate().getUseLimits().getDelayId());
				PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_CANCELED(parentItem.getNameId()));
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem
					.getItemTemplate().getTemplateId(), 0, 2, 0), true);
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
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_SUCCEED(parentItem.getNameId()));
					if (selectedCollection.getItems().size() > 0) {
						for (ResultedItem resultItem : selectedCollection.getItems()) {
							if (resultItem.isObtainableFor(player)) {
								ItemService.addItem(player, resultItem.getItemId(), resultItem.getResultCount(), true,
									new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_ITEM_COLLECT));
							}
						}
					}
					if (selectedCollection.getRandomItems().size() > 0) {
						for (RandomItem randomItem : selectedCollection.getRandomItems()) {
							RandomType randomType = randomItem.getType();
							if (randomType != null) {
								int randomId = 0;
								int i = 0;
								int itemLvl = parentItem.getItemTemplate().getLevel();
								switch (randomItem.getType()) {
									case ENCHANTMENT: {
										do {
											randomId = 166000191 + Math.round(itemLvl / 100f) + Rnd.get(4);
											i++;
											if (i > 50) {
												randomId = 0;
												log.warn("DecomposeAction random item id not found. " + parentItem.getItemId());
												break;
											}
										} while (!ItemService.checkRandomTemplate(randomId));
										break;
									}
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
										if (manastones == null) {
											manastones = DataManager.ITEM_DATA.getManastones();
										}
										if (randomType.equals(RandomType.MANASTONE)) // stone level near or equal to item level (if 1, near player level) 
											itemLvl = itemLvl % 10 == 0 ? itemLvl : ((int) Math.ceil((itemLvl == 1 ? player.getLevel() : itemLvl) / 10f) * 10);
										else
											itemLvl = randomType.getLevel();
										List<ItemTemplate> stones = manastones.get(itemLvl);
										if (stones == null) {
											log.warn("DecomposeAction random item id not found. " + parentItem.getItemTemplate().getTemplateId());
											break;
										}
										if (!randomType.equals(RandomType.MANASTONE)) {
											ItemQuality itemQuality = ItemQuality.COMMON;
											if (randomType.name().contains("RARE")) {
												itemQuality = ItemQuality.RARE;
											} else if (randomType.name().contains("LEGEND")) {
												itemQuality = ItemQuality.LEGEND;
											}
											List<ItemTemplate> selectedStones = select(stones, having(on(ItemTemplate.class).getItemQuality(), equalTo(itemQuality)));
											randomId = selectedStones.get(Rnd.get(selectedStones.size())).getTemplateId();
										} else {
											List<ItemTemplate> selectedStones = select(stones,
												having(on(ItemTemplate.class).getItemQuality(), not(equalTo(ItemQuality.LEGEND))));
											randomId = selectedStones.get(Rnd.get(selectedStones.size())).getTemplateId();
										}

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									case SPECIAL_MANASTONE_RARE_GRADE:
									case SPECIAL_MANASTONE_LEGEND_GRADE:
									case SPECIAL_MANASTONE_UNIQUE_GRADE:
									case SPECIAL_MANASTONE_EPIC_GRADE:
										if (specialManastones == null) {
											specialManastones = DataManager.ITEM_DATA.getSpecialManastones();
										}
										List<ItemTemplate> ancientStones = specialManastones.get(randomType.getLevel());
										if (ancientStones == null) {
											log.warn("DecomposeAction random item id not found. " + parentItem.getItemTemplate().getTemplateId());
											break;
										}
										ItemQuality itemQuality = ItemQuality.COMMON;
										if (randomType.name().contains("RARE"))
											itemQuality = ItemQuality.RARE;
										else if (randomType.name().contains("LEGEND"))
											itemQuality = ItemQuality.LEGEND;
										else if (randomType.name().contains("UNIQUE"))
											itemQuality = ItemQuality.UNIQUE;
										else if (randomType.name().contains("EPIC"))
											itemQuality = ItemQuality.EPIC;
										List<ItemTemplate> selectedStones = select(ancientStones, having(on(ItemTemplate.class).getItemQuality(), equalTo(itemQuality)));
										randomId = selectedStones.get(Rnd.get(selectedStones.size())).getTemplateId();

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									case CHUNK_EARTH: {
										int[] earth = chunkEarth.get(player.getRace());

										randomId = earth[Rnd.get(earth.length)];
										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
									case CHUNK_SAND: {
										int[] sand = chunkSand.get(player.getRace());

										randomId = sand[Rnd.get(sand.length)];

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
									case CHUNK_ROCK: {
										randomId = chunkRock[Rnd.get(chunkRock.length)];

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
									case CHUNK_GEMSTONE: {
										randomId = chunkGemstone[Rnd.get(chunkGemstone.length)];

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
									case SCROLLS: {
										randomId = scrolls[Rnd.get(scrolls.length)];

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
									case POTION: {
										randomId = potion[Rnd.get(potion.length)];

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
									case ANCIENTITEMS: {
										do {
											randomId = Rnd.get(186000051, 186000066);
											i++;
											if (i > 50) {
												randomId = 0;
												log.warn("DecomposeAction random item id not found. " + parentItem.getItemId());
												break;
											}
										} while (!ItemService.checkRandomTemplate(randomId));
										break;
									}
									case ANCIENT_CROWN: {
										do {
											randomId = Rnd.get(186000051, 186000054);
											i++;
											if (i > 50) {
												randomId = 0;
												log.warn("DecomposeAction random item id not found. " + parentItem.getItemId());
												break;
											}
										} while (!ItemService.checkRandomTemplate(randomId));
										break;
									}
									case ANCIENT_GOBLET: {
										do {
											randomId = Rnd.get(186000055, 186000058);
											i++;
											if (i > 50) {
												randomId = 0;
												log.warn("DecomposeAction random item id not found. " + parentItem.getItemId());
												break;
											}
										} while (!ItemService.checkRandomTemplate(randomId));
										break;
									}
									case ANCIENT_SEAL: {
										do {
											randomId = Rnd.get(186000059, 186000062);
											i++;
											if (i > 50) {
												randomId = 0;
												log.warn("DecomposeAction random item id not found. " + parentItem.getItemId());
												break;
											}
										} while (!ItemService.checkRandomTemplate(randomId));
										break;
									}
									case ANCIENT_ICON: {
										do {
											randomId = Rnd.get(186000063, 186000066);
											i++;
											if (i > 50) {
												randomId = 0;
												log.warn("DecomposeAction random item id not found. " + parentItem.getItemId());
												break;
											}
										} while (!ItemService.checkRandomTemplate(randomId));
										break;
									}
									case LESSER_POTIONS: {
										randomId = lesser_potions[Rnd.get(lesser_potions.length)];

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
									case POTION_50: {
										randomId = potion_50[Rnd.get(potion_50.length)];

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
									case ILLUSION_GODSTONE: {
										randomId = illusion_godstones[Rnd.get(illusion_godstones.length)];

										if (!ItemService.checkRandomTemplate(randomId)) {
											log.warn("DecomposeAction random item id not found. " + randomId);
											return;
										}
										break;
									}
								}
								if (randomId != 0 && randomId != 167000524)
									ItemService.addItem(player, randomId, randomItem.getResultCount(), true, new ItemUpdatePredicate(ItemAddType.DECOMPOSABLE, ItemUpdateType.INC_ITEM_COLLECT));
							}
						}
					}
				}
				PacketSendUtility.broadcastPacketAndReceive(player,
					new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), parentItem.getObjectId(), parentItem.getItemId(), 0, validAction ? 1 : 2, 0));
			}

			boolean postValidate(Player player, Item parentItem) {
				if (!canAct(player, parentItem, targetItem)) {
					return false;
				}
				Storage inventory = player.getInventory();
				int slotReq = calcMaxCountOfSlots(selectedCollection, player, false);
				int specialSlotreq = calcMaxCountOfSlots(selectedCollection, player, true);
				if (slotReq > 0 && inventory.getFreeSlots() < slotReq) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_INVENTORY_IS_FULL());
					return false;
				}
				if (specialSlotreq > 0 && inventory.getSpecialCubeFreeSlots() < specialSlotreq) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_INVENTORY_IS_FULL());
					return false;
				}
				if (player.getLifeStats().isAlreadyDead() || !player.isSpawned()) {
					return false;
				}
				if (!player.getInventory().decreaseByObjectId(parentItem.getObjectId(), 1)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_NO_TARGET_ITEM());
					return false;
				}
				if (selectedCollection.getItems().isEmpty() && selectedCollection.getRandomItems().isEmpty()) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_DECOMPOSE_ITEM_FAILED(parentItem.getNameId()));
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
		Collection<ExtractedItemsCollection> result = new FastTable<ExtractedItemsCollection>();
		for (ExtractedItemsCollection collection : itemsCollections) {
			if (collection.getMinLevel() > playerLevel) {
				continue;
			}
			if (collection.getMaxLevel() > 0 && collection.getMaxLevel() < playerLevel) {
				continue;
			}
			result.add(collection);
		}
		return result;
	}

	/**
	 * Select only 1 item based on chance attributes
	 */
	private ExtractedItemsCollection selectItemByChance(Collection<ExtractedItemsCollection> itemsCollections) {
		if (itemsCollections == null) {
			return null;
		}
		float sumOfChances = calcSumOfChances(itemsCollections);
		float currentSum = 0f;
		float rnd = (float) Rnd.get(0, (int) (sumOfChances - 1) * 1000) / 1000;
		ExtractedItemsCollection selectedCollection = null;
		for (ExtractedItemsCollection collection : itemsCollections) {
			currentSum += collection.getChance();
			if (rnd < currentSum) {
				selectedCollection = collection;
				break;
			}
		}
		return selectedCollection;
	}

	private int calcMaxCountOfSlots(ExtractedItemsCollection itemsCollections, Player player, boolean special) {
		int maxCount = 0;
		for (ResultedItem item : itemsCollections.getItems()) {
			if (item.isObtainableFor(player)) {
				ItemTemplate template = DataManager.ITEM_DATA.getItemTemplate(item.getItemId());
				if (special && template.getExtraInventoryId() > 0) {
					maxCount++;
				} else if (template.getExtraInventoryId() < 1) {
					maxCount++;
				}
			}
		}
		return maxCount;
	}

	private float calcSumOfChances(Collection<ExtractedItemsCollection> itemsCollections) {
		float sum = 0;
		for (ExtractedItemsCollection collection : itemsCollections)
			sum += collection.getChance();
		return sum;
	}

}
