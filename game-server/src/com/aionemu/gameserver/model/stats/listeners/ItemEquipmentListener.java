package com.aionemu.gameserver.model.stats.listeners;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.enchants.EnchantEffect;
import com.aionemu.gameserver.model.enchants.EnchantStat;
import com.aionemu.gameserver.model.enchants.TemperingEffect;
import com.aionemu.gameserver.model.enchants.TemperingStat;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.RandomStats;
import com.aionemu.gameserver.model.stats.calc.functions.StatAddFunction;
import com.aionemu.gameserver.model.stats.calc.functions.StatFunction;
import com.aionemu.gameserver.model.stats.container.CreatureGameStats;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.model.templates.item.WeaponStats;
import com.aionemu.gameserver.model.templates.item.enums.ItemGroup;
import com.aionemu.gameserver.model.templates.itemset.FullBonus;
import com.aionemu.gameserver.model.templates.itemset.ItemSetTemplate;
import com.aionemu.gameserver.model.templates.itemset.PartBonus;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SKILL_COOLDOWN;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xavier modified by Wakizashi
 */
public class ItemEquipmentListener {

	/**
	 * @param item
	 * @param cgs
	 */
	public static void onItemEquipment(Item item, Player owner) {
		owner.getController().cancelUseItem();
		ItemTemplate itemTemplate = item.getItemTemplate();

		onItemEquipment(item, owner.getGameStats());

		// Check if belongs to ItemSet
		if (itemTemplate.isItemSet()) {
			recalculateItemSet(itemTemplate.getItemSet(), owner);
		}
		if (item.hasManaStones())
			addStonesStats(item, item.getItemStones(), owner.getGameStats());

		if (item.hasFusionStones())
			addStonesStats(item, item.getFusionStones(), owner.getGameStats());
		IdianStone idianStone = item.getIdianStone();
		if (idianStone != null) {
			idianStone.onEquip(owner, item.getEquipmentSlot());
		}
		if (item.getBuffSkill() != 0) {
			SkillTemplate buffSkill = DataManager.SKILL_DATA.getSkillTemplate(item.getBuffSkill());
			SkillLearnService.addSkill(owner, item.getBuffSkill());
			HashMap<Integer, Long> coolDowns = new HashMap<>();
			long currTime = System.currentTimeMillis();
			long oldCooldown = owner.getSkillCoolDown(buffSkill.getCooldownId());
			long newCooldown = 0;
			if (oldCooldown - currTime > 15000) // cd active
				newCooldown = oldCooldown;
			else
				newCooldown = currTime + 15000;
			owner.setSkillCoolDown(buffSkill.getCooldownId(), newCooldown);
			coolDowns.put(buffSkill.getCooldownId(), newCooldown);
			PacketSendUtility.sendPacket(owner, new SM_SKILL_COOLDOWN(coolDowns));
		}
		addGodstoneEffect(owner, item);
		RandomStats randomStats = item.getRandomStats();
		if (randomStats != null) {
			randomStats.onEquip(owner);
		}
		if (item.getConditioningInfo() != null) {
			owner.getObserveController().addObserver(item.getConditioningInfo());
			item.getConditioningInfo().setPlayer(owner);
		}
		int enchantLevel = item.getEnchantLevel();
		int temperingLevel = item.getTempering();
		if (enchantLevel > 0) {
			ItemGroup itemGroup = itemTemplate.getItemGroup();
			HashMap<Integer, List<EnchantStat>> enchant = DataManager.ENCHANT_DATA.getTemplates(itemGroup);
			if (enchant != null) {
				item.setEnchantEffect(new EnchantEffect(item, owner, enchant.get(enchantLevel)));
			}
		}
		if (temperingLevel > 0) {
			if (item.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
				item.setTemperingEffect(new TemperingEffect(owner, item));
			} else {
				HashMap<Integer, List<TemperingStat>> tempering = DataManager.TEMPERING_DATA.getTemplates(itemTemplate.getItemGroup());
				if (tempering != null) {
					item.setTemperingEffect(new TemperingEffect(owner, tempering.get(temperingLevel)));
				}
			}
		}
	}

	/**
	 * @param item
	 * @param owner
	 */
	public static void onItemUnequipment(Item item, Player owner) {
		owner.getController().cancelUseItem();

		ItemTemplate itemTemplate = item.getItemTemplate();
		// Check if belongs to ItemSet
		if (itemTemplate.isItemSet()) {
			recalculateItemSet(itemTemplate.getItemSet(), owner);
		}

		owner.getGameStats().endEffect(item);

		if (item.hasManaStones())
			removeStoneStats(item.getItemStones(), owner.getGameStats());

		if (item.hasFusionStones())
			removeStoneStats(item.getFusionStones(), owner.getGameStats());

		if (item.getConditioningInfo() != null) {
			owner.getObserveController().removeObserver(item.getConditioningInfo());
			item.getConditioningInfo().setPlayer(null);
		}
		IdianStone idianStone = item.getIdianStone();
		if (idianStone != null) {
			idianStone.onUnEquip(owner);
		}
		removeGodstoneEffect(owner, item);
		RandomStats randomStats = item.getRandomStats();
		if (randomStats != null) {
			randomStats.onUnEquip(owner);
		}
		if (item.getEnchantEffect() != null) {
			item.getEnchantEffect().endEffect(owner);
			item.setEnchantEffect(null);
		}
		if (item.getTemperingEffect() != null) {
			item.getTemperingEffect().endEffect(owner);
			item.setTemperingEffect(null);
		}
		if (item.getBuffSkill() != 0) {
			SkillLearnService.removeSkill(owner, item.getBuffSkill());
		}
	}

	/**
	 * @param itemTemplate
	 * @param slot
	 * @param cgs
	 */
	private static void onItemEquipment(Item item, CreatureGameStats<?> cgs) {
		ItemTemplate itemTemplate = item.getItemTemplate();
		long slot = item.getEquipmentSlot();
		List<StatFunction> modifiers = itemTemplate.getModifiers();
		if (modifiers == null) {
			return;
		}

		List<StatFunction> allModifiers = null;

		if ((slot & ItemSlot.MAIN_OR_SUB.getSlotIdMask()) != 0) {
			allModifiers = wrapModifiers(item, modifiers);
			if (item.hasFusionedItem()) {
				// add all bonus modifiers according to rules
				ItemTemplate fusionedItemTemplate = item.getFusionedItemTemplate();
				ItemGroup weaponType = fusionedItemTemplate.getItemGroup();
				List<StatFunction> fusionedItemModifiers = fusionedItemTemplate.getModifiers();
				if (fusionedItemModifiers != null) {
					allModifiers.addAll(wrapModifiers(item, fusionedItemModifiers));
				}
				// add 10% of Magic Boost and Attack
				WeaponStats weaponStats = fusionedItemTemplate.getWeaponStats();
				if (weaponStats != null) {
					int boostMagicalSkill = Math.round(0.1f * weaponStats.getBoostMagicalSkill());
					int attack = Math.round(0.1f * weaponStats.getMeanDamage());
					if (weaponType == ItemGroup.ORB || weaponType == ItemGroup.STAFF || weaponType == ItemGroup.SPELLBOOK || weaponType == ItemGroup.GUN
						|| weaponType == ItemGroup.CANNON || weaponType == ItemGroup.HARP || weaponType == ItemGroup.KEYBLADE) {
						allModifiers.add(new StatAddFunction(StatEnum.BOOST_MAGICAL_SKILL, boostMagicalSkill, false));
					}
					allModifiers.add(new StatAddFunction(item.getItemTemplate().getAttackType().isMagical() ? StatEnum.MAGICAL_ATTACK
						: StatEnum.PHYSICAL_ATTACK, attack, false));
				}
			}
		} else {
			allModifiers = modifiers;
		}
		item.setCurrentModifiers(allModifiers);
		cgs.addEffect(item, allModifiers);
	}

	/**
	 * Filter stats based on the following rules:<br>
	 * 1) don't include fusioned stats which will be taken only from 1 weapon <br>
	 * 2) wrap stats which are different for MAIN and OFF hands<br>
	 * 3) add the rest<br>
	 *
	 * @param item
	 * @param modifiers
	 * @return
	 */
	private static List<StatFunction> wrapModifiers(Item item, List<StatFunction> modifiers) {
		List<StatFunction> allModifiers = new ArrayList<StatFunction>();
		for (StatFunction modifier : modifiers) {
			switch (modifier.getName()) {
			// why they are removed look at DuplicateStatFunction
				case ATTACK_SPEED:
				case PVP_ATTACK_RATIO:
				case BOOST_CASTING_TIME:
					continue;
				default:
					allModifiers.add(modifier);
			}
		}
		return allModifiers;
	}

	/**
	 * @param itemSetTemplate
	 * @param player
	 * @param isWeapon
	 */
	private static void recalculateItemSet(ItemSetTemplate itemSetTemplate, Player player) {
		if (itemSetTemplate == null)
			return;

		// TODO quite
		player.getGameStats().endEffect(itemSetTemplate);
		// 1.- Check equipment for items already equip with this itemSetTemplate id
		int itemSetPartsEquipped = player.getEquipment().itemSetPartsEquipped(itemSetTemplate.getId());

		// 2.- Check Item Set Parts and add effects one by one if not done already
		for (PartBonus itempartbonus : itemSetTemplate.getPartbonus()) {
			if (itempartbonus.getCount() <= itemSetPartsEquipped) {
				player.getGameStats().addEffect(itemSetTemplate, itempartbonus.getModifiers());
			}
		}

		// 3.- Finally check if all items are applied and set the full bonus if not already applied
		FullBonus fullbonus = itemSetTemplate.getFullbonus();
		if (fullbonus != null && itemSetPartsEquipped == fullbonus.getCount()) {
			// Add the full bonus with index = total parts + 1 to avoid confusion with part bonus equal to number of
			// objects
			player.getGameStats().addEffect(itemSetTemplate, fullbonus.getModifiers());
		}
	}

	/**
	 * All modifiers of stones will be applied to character
	 *
	 * @param item
	 * @param cgs
	 */
	private static void addStonesStats(Item item, Set<? extends ManaStone> itemStones, CreatureGameStats<?> cgs) {
		if (itemStones == null || itemStones.size() == 0)
			return;

		for (ManaStone stone : itemStones) {
			addStoneStats(item, stone, cgs);
		}
	}

	/**
	 * Used when socketing of equipped item
	 *
	 * @param item
	 * @param stone
	 * @param cgs
	 */
	public static void addStoneStats(Item item, ManaStone stone, CreatureGameStats<?> cgs) {
		if (stone == null)
			return;
		List<StatFunction> modifiers = stone.getModifiers();
		if (modifiers == null) {
			return;
		}

		cgs.addEffect(stone, modifiers);
	}

	/**
	 * All modifiers of stones will be removed
	 *
	 * @param itemStones
	 * @param cgs
	 */
	public static void removeStoneStats(Set<? extends ManaStone> itemStones, CreatureGameStats<?> cgs) {
		if (itemStones == null || itemStones.size() == 0)
			return;

		for (ManaStone stone : itemStones) {
			List<StatFunction> modifiers = stone.getModifiers();
			if (modifiers != null) {
				cgs.endEffect(stone);
			}
		}
	}

	/**
	 * @param item
	 */
	private static void addGodstoneEffect(Player player, Item item) {
		if (item.getGodStone() != null) {
			item.getGodStone().onEquip(player);
		}
	}

	/**
	 * @param item
	 */
	private static void removeGodstoneEffect(Player player, Item item) {
		if (item.getGodStone() != null) {
			item.getGodStone().onUnEquip(player);
		}
	}

}
