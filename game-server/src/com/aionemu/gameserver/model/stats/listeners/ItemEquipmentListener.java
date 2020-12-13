package com.aionemu.gameserver.model.stats.listeners;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.function.Consumer;

import org.slf4j.LoggerFactory;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.enchants.TemperingEffect;
import com.aionemu.gameserver.model.enchants.TemperingStat;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.IdianStone;
import com.aionemu.gameserver.model.items.ItemSlot;
import com.aionemu.gameserver.model.items.ManaStone;
import com.aionemu.gameserver.model.items.RandomBonusEffect;
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
import com.aionemu.gameserver.services.EnchantService;
import com.aionemu.gameserver.services.SkillLearnService;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author xavier, Wakizashi
 */
public class ItemEquipmentListener {

	public static void onItemEquipment(Item item, Player owner) {
		owner.getController().cancelUseItem();
		ItemTemplate itemTemplate = item.getItemTemplate();

		addWeaponStats(item, owner.getGameStats());

		if (itemTemplate.isItemSet())
			recalculateItemSet(itemTemplate.getItemSet(), owner);
		if (item.hasManaStones())
			addStonesStats(item, item.getItemStones(), owner.getGameStats());
		if (item.hasFusionStones())
			addStonesStats(item, item.getFusionStones(), owner.getGameStats());

		IdianStone idianStone = item.getIdianStone();
		if (idianStone != null)
			idianStone.onEquip(owner, item.getEquipmentSlot());

		if (item.getBuffSkill() != 0) {
			SkillTemplate buffSkill = DataManager.SKILL_DATA.getSkillTemplate(item.getBuffSkill());
			SkillLearnService.learnTemporarySkill(owner, item.getBuffSkill(), 1);
			Map<Integer, Long> coolDowns = new HashMap<>();
			long currTime = System.currentTimeMillis();
			long oldCooldown = owner.getSkillCoolDown(buffSkill.getCooldownId());
			long newCooldown;
			if (oldCooldown - currTime > 15000) // cd active
				newCooldown = oldCooldown;
			else
				newCooldown = currTime + 15000;
			owner.setSkillCoolDown(buffSkill.getCooldownId(), newCooldown);
			coolDowns.put(buffSkill.getCooldownId(), newCooldown);
			PacketSendUtility.sendPacket(owner, new SM_SKILL_COOLDOWN(coolDowns));
		}
		forEachBonusStats(bonusStats -> bonusStats.applyEffect(owner), item.getBonusStatsEffect(), item.getFusionedItemBonusStatsEffect());
		if (item.getConditioningInfo() != null) {
			owner.getObserveController().addObserver(item.getConditioningInfo());
			item.getConditioningInfo().setPlayer(owner);
		}
		int enchantLevel = item.getEnchantLevel();
		int temperingLevel = item.getTempering();
		if (enchantLevel > 0)
			EnchantService.applyEnchantEffect(item, owner, enchantLevel);
		if (temperingLevel > 0) {
			if (item.getItemTemplate().getItemGroup() == ItemGroup.PLUME) {
				item.setTemperingEffect(new TemperingEffect(owner, item));
			} else {
				Map<Integer, List<TemperingStat>> tempering = DataManager.TEMPERING_DATA.getTemplates(itemTemplate);
				if (tempering != null) {
					List<TemperingStat> temperingStats = tempering.get(temperingLevel);
					if (temperingStats != null)
						item.setTemperingEffect(new TemperingEffect(owner, temperingStats));
					else
						LoggerFactory.getLogger(ItemEquipmentListener.class)
							.warn("Missing tempering effect info for item " + itemTemplate.getTemplateId() + " on +" + temperingLevel);
				}
			}
		}
	}

	private static void forEachBonusStats(Consumer<RandomBonusEffect> action, RandomBonusEffect... bonusStatsEffects) {
		for (RandomBonusEffect bonusStats : bonusStatsEffects)
			if (bonusStats != null)
				action.accept(bonusStats);
	}

	public static void onItemUnequipment(Item item, Player owner) {
		owner.getController().cancelUseItem();

		ItemTemplate itemTemplate = item.getItemTemplate();
		// Check if belongs to ItemSet
		if (itemTemplate.isItemSet())
			recalculateItemSet(itemTemplate.getItemSet(), owner);

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
		if (idianStone != null)
			idianStone.onUnEquip(owner);
		forEachBonusStats(bonusStats -> bonusStats.endEffect(owner), item.getBonusStatsEffect(), item.getFusionedItemBonusStatsEffect());
		if (item.getEnchantEffect() != null) {
			item.getEnchantEffect().endEffect(owner);
			item.setEnchantEffect(null);
		}
		if (item.getTemperingEffect() != null) {
			item.getTemperingEffect().endEffect(owner);
			item.setTemperingEffect(null);
		}
		if (item.getBuffSkill() != 0)
			SkillLearnService.removeSkill(owner, item.getBuffSkill());
	}

	private static void addWeaponStats(Item item, CreatureGameStats<?> cgs) {
		ItemTemplate itemTemplate = item.getItemTemplate();
		List<StatFunction> mainWeaponModifiers = itemTemplate.getModifiers();
		if (mainWeaponModifiers == null)
			mainWeaponModifiers = Collections.emptyList();

		List<StatFunction> modifiersToApply;
		if ((item.getEquipmentSlot() & ItemSlot.MAIN_OR_SUB.getSlotIdMask()) != 0) {
			modifiersToApply = extractApplicableWeaponModifiers(item, mainWeaponModifiers);
			if (item.hasFusionedItem()) {
				// add all bonus modifiers according to rules
				ItemTemplate fusionedItemTemplate = item.getFusionedItemTemplate();
				ItemGroup weaponType = fusionedItemTemplate.getItemGroup();
				List<StatFunction> fusionedItemModifiers = fusionedItemTemplate.getModifiers();
				if (fusionedItemModifiers != null)
					modifiersToApply.addAll(extractApplicableWeaponModifiers(item, fusionedItemModifiers));

				// add 10% of Magic Boost and Attack
				WeaponStats weaponStats = fusionedItemTemplate.getWeaponStats();
				if (weaponStats != null) {
					int boostMagicalSkill = (int) (0.1f * weaponStats.getBoostMagicalSkill());
					int attack = (int) (0.1f * weaponStats.getMeanDamage());
					if (weaponType == ItemGroup.ORB || weaponType == ItemGroup.STAFF || weaponType == ItemGroup.SPELLBOOK || weaponType == ItemGroup.GUN
						|| weaponType == ItemGroup.CANNON || weaponType == ItemGroup.HARP || weaponType == ItemGroup.KEYBLADE) {
						modifiersToApply.add(new StatAddFunction(StatEnum.BOOST_MAGICAL_SKILL, boostMagicalSkill, false));
					}
					modifiersToApply.add(new StatAddFunction(
						item.getItemTemplate().getAttackType().isMagical() ? StatEnum.MAGICAL_ATTACK : StatEnum.PHYSICAL_ATTACK, attack, false));
				}
			}
		} else {
			modifiersToApply = mainWeaponModifiers;
		}
		item.setCurrentModifiers(modifiersToApply);
		cgs.addEffect(item, modifiersToApply);
	}

	private static List<StatFunction> extractApplicableWeaponModifiers(Item item, List<StatFunction> modifiers) {
		List<StatFunction> allModifiers = new ArrayList<>();
		for (StatFunction modifier : modifiers) {
			switch (modifier.getName()) {
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

	private static void recalculateItemSet(ItemSetTemplate itemSetTemplate, Player player) {
		if (itemSetTemplate == null)
			return;

		// TODO quite
		player.getGameStats().endEffect(itemSetTemplate);
		// 1.- Check equipment for items already equip with this itemSetTemplate id
		int itemSetPartsEquipped = player.getEquipment().itemSetPartsEquipped(itemSetTemplate.getId());

		// 2.- Check Item Set Parts and add effects one by one if not done already
		for (PartBonus itempartbonus : itemSetTemplate.getPartbonus())
			if (itempartbonus.getCount() <= itemSetPartsEquipped)
				player.getGameStats().addEffect(itemSetTemplate, itempartbonus.getModifiers());

		// 3.- Finally check if all items are applied and set the full bonus if not already applied
		FullBonus fullbonus = itemSetTemplate.getFullbonus();
		if (fullbonus != null && itemSetPartsEquipped == fullbonus.getCount()) {
			// Add the full bonus with index = total parts + 1 to avoid confusion with part bonus equal to number of
			// objects
			player.getGameStats().addEffect(itemSetTemplate, fullbonus.getModifiers());
		}
	}

	private static void addStonesStats(Item item, Set<? extends ManaStone> itemStones, CreatureGameStats<?> cgs) {
		if (itemStones == null || itemStones.isEmpty())
			return;
		for (ManaStone stone : itemStones)
			addStoneStats(item, stone, cgs);
	}

	public static void addStoneStats(Item item, ManaStone stone, CreatureGameStats<?> cgs) {
		if (stone == null || stone.getModifiers() == null)
			return;
		cgs.addEffect(stone, stone.getModifiers());
	}

	public static void removeStoneStats(Set<? extends ManaStone> itemStones, CreatureGameStats<?> cgs) {
		if (itemStones == null || itemStones.isEmpty())
			return;
		for (ManaStone stone : itemStones) {
			List<StatFunction> modifiers = stone.getModifiers();
			if (modifiers != null)
				cgs.endEffect(stone);
		}
	}

}
