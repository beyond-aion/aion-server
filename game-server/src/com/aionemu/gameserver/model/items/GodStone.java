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

	private final Item parentItem;
	private final GodstoneInfo godstoneInfo;
	private int activatedCount;
	private ActionObserver actionListener;

	public GodStone(Item parentItem, int activatedCount, int itemId, PersistentState persistentState) {
		super(parentItem.getObjectId(), itemId, 0, persistentState);
		this.parentItem = parentItem;
		this.godstoneInfo = DataManager.ITEM_DATA.getItemTemplate(itemId).getGodstoneInfo();
		this.activatedCount = activatedCount;
		if (godstoneInfo == null)
			log.warn("Godstone info is missing for item: " + itemId);
	}

	public void onEquip(Player player) {
		if (godstoneInfo == null)
			return;
		if (actionListener != null)
			onUnEquip(player);
		actionListener = new ActionObserver(ObserverType.GODSTONE) {

			private final Player owner = player;
			private final int handProbability = (parentItem.getEquipmentSlot() & ItemSlot.MAIN_HAND.getSlotIdMask()) != 0 ? godstoneInfo.getProbability()
				: godstoneInfo.getProbabilityLeft();

			@Override
			public void calculateGodstoneChance(Creature creature) {
				if (creature == null)
					return;
				int procProbability = handProbability;
				if (creature instanceof Player) {
					procProbability -= ((Player) creature).getGameStats().getStat(StatEnum.PROC_REDUCE_RATE, 0).getCurrent();
				}
				if (Rnd.get(1, 1000) <= procProbability) {
					ItemTemplate itemTemplate = DataManager.ITEM_DATA.getItemTemplate(getItemId());
					Skill skill = SkillEngine.getInstance().getSkill(owner, godstoneInfo.getSkillid(), godstoneInfo.getSkilllvl(), creature, itemTemplate,
						false);
					skill.setFirstTargetRangeCheck(false);
					if (skill.canUseSkill(CastState.CAST_START)) {
						PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_SKILL_PROC_EFFECT_OCCURRED(skill.getSkillTemplate().getNameId()));
						Effect effect = new Effect(owner, creature, skill.getSkillTemplate(), 1, 0, itemTemplate);
						effect.initialize();
						effect.applyEffect();
						// Illusion Godstones
						if (godstoneInfo.getBreakProb() > 0) {
							increaseActivatedCount();
							if (activatedCount > godstoneInfo.getNonBreakCount() && Rnd.get(1, 1000) <= godstoneInfo.getBreakProb()) {
								// TODO: Delay 10 Minutes, send messages etc
								// PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_MSG_BREAK_PROC_REMAIN_START(equippedItem.getNameId(),
								// itemTemplate.getNameId()));
								breakGodstone(owner, itemTemplate.getNameId());
							}
						}
					}
				}
			}

			private void breakGodstone(Player owner, int godStoneNameId) {
				PacketSendUtility.sendPacket(owner, SM_SYSTEM_MESSAGE.STR_MSG_BREAK_PROC(parentItem.getNameId(), godStoneNameId));
				GodStone.this.setPersistentState(PersistentState.DELETED);
				onUnEquip(owner);
				parentItem.setGodStone(null);
				DAOManager.getDAO(ItemStoneListDAO.class).save(Collections.singletonList(parentItem));
				ItemPacketService.updateItemAfterInfoChange(owner, parentItem);
			}
		};
		player.getObserveController().addObserver(actionListener);
	}

	/**
	 * @param player
	 */
	public void onUnEquip(Player player) {
		if (actionListener != null) {
			player.getObserveController().removeObserver(actionListener);
			actionListener = null;
		}
	}

	private void increaseActivatedCount() {
		activatedCount++;
		setPersistentState(PersistentState.UPDATE_REQUIRED);
	}

	public int getActivatedCount() {
		return activatedCount;
	}

}
