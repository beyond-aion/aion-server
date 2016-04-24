package com.aionemu.gameserver.model.items;

import java.util.Collections;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.aionemu.commons.database.dao.DAOManager;
import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.controllers.observer.ActionObserver;
import com.aionemu.gameserver.controllers.observer.ObserverType;
import com.aionemu.gameserver.dao.ItemStoneListDAO;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.StatEnum;
import com.aionemu.gameserver.model.templates.item.GodstoneInfo;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.item.ItemPacketService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
public class GodStone extends ItemStone {

	private static final Logger log = LoggerFactory.getLogger(GodStone.class);

	private final GodstoneInfo godstoneInfo;
	private ActionObserver actionListener;
	private final int probability;
	private final int probabilityLeft;
	private final int breakProb;
	private final int nonBreakCount;
	private final ItemTemplate godItem;
	private int activatedCount;

	public GodStone(int itemObjId, int activatedCount, int itemId, PersistentState persistentState) {
		super(itemObjId, itemId, 0, persistentState);
		ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(itemId);
		godItem = itemTemplate;
		godstoneInfo = itemTemplate.getGodstoneInfo();

		if (godstoneInfo != null) {
			probability = godstoneInfo.getProbability();
			probabilityLeft = godstoneInfo.getProbabilityleft();
			breakProb = godstoneInfo.getBreakProb();
			nonBreakCount = godstoneInfo.getNonBreakCount();
			this.activatedCount = activatedCount;
		} else {
			probability = 0;
			probabilityLeft = 0;
			breakProb = 0;
			nonBreakCount = 0;
			log.warn("CHECKPOINT: Godstone info missing for item : " + itemId);
		}

	}

	public void onEquip(final Player player) {
		if (godstoneInfo == null || godItem == null)
			return;

		Item equippedItem = player.getEquipment().getEquippedItemByObjId(getItemObjId());
		long equipmentSlot = equippedItem.getEquipmentSlot();
		final int handProbability = (equipmentSlot & ItemSlot.MAIN_HAND.getSlotIdMask()) != 0 ? probability : probabilityLeft;
		actionListener = new ActionObserver(ObserverType.ATTACK) {

			@Override
			public void attack(Creature creature) {
				int procProbability = handProbability;
				if (creature instanceof Player) {
					procProbability -= ((Player)creature).getGameStats().getStat(StatEnum.PROC_REDUCE_RATE, 0).getCurrent();
				}
				if (Rnd.get(1, 1000) <= procProbability) {
					Skill skill = SkillEngine.getInstance().getSkill(player, godstoneInfo.getSkillid(), godstoneInfo.getSkilllvl(), creature,
						godItem, false);
					skill.setFirstTargetRangeCheck(false);
					if (skill.canUseSkill(CastState.CAST_START)) {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_PROC_EFFECT_OCCURRED(skill.getSkillTemplate().getNameId()));
						Effect effect = new Effect(player, creature, skill.getSkillTemplate(), 1, 0, godItem);
						effect.initialize();
						effect.applyEffect();
						effect = null;
						// Illusion Godstones
						if (breakProb > 0) {
							increaseActivatedCount();
							if (activatedCount > nonBreakCount && Rnd.get(1, 1000) <= breakProb) {
								// TODO: Delay 10 Minutes, send messages etc
								// PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_BREAK_PROC_REMAIN_START(equippedItem.getNameId(),
								// godItem.getNameId()));
								breakGodstone(player, equippedItem);
							}
						}
					}
				}
			}
		};

		player.getObserveController().addObserver(actionListener);
	}

	/**
	 * @param player
	 */
	public void onUnEquip(Player player) {
		if (actionListener != null)
			player.getObserveController().removeObserver(actionListener);
	}

	private void increaseActivatedCount() {
		this.activatedCount++;
		this.setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getActivatedCount() {
		return this.activatedCount;
	}

	private void breakGodstone(Player player, Item item) {
		PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_BREAK_PROC(item.getNameId(), godItem.getNameId()));
		this.setPersistentState(PersistentState.DELETED);
		onUnEquip(player);
		item.setGodStone(null);
		DAOManager.getDAO(ItemStoneListDAO.class).save(Collections.singletonList(item));
		ItemPacketService.updateItemAfterInfoChange(player, item);
	}
}
