package com.aionemu.gameserver.model.templates.item.actions;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.*;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.skillengine.properties.Properties.CastState;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "SkillUseAction")
public class SkillUseAction extends AbstractItemAction {

	@XmlAttribute
	protected int skillid;

	@XmlAttribute
	protected int level;

	@XmlAttribute(required = false)
	private int mapid;

	/**
	 * Gets the value of the skillid property.
	 */
	public int getSkillId() {
		return skillid;
	}

	/**
	 * Gets the value of the level property.
	 */
	public int getLevel() {
		return level;
	}

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem, Object... params) {
		Skill skill = SkillEngine.getInstance().getSkill(player, skillid, level, player.getTarget(), parentItem.getItemTemplate());
		if (skill == null)
			return false;
		List<EffectTemplate> effects = skill.getSkillTemplate().getEffects().getEffects();
		if (!effects.isEmpty()) {
			for (EffectTemplate template : effects) {
				if (player.isTransformed() && template instanceof TransformEffect) { // Cant use transform items while already transformed
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_CAN_NOT_CAST_IN_SHAPECHANGE());
					return false;
				}
				if (player.getSummon() != null && template instanceof SummonEffect) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_SUMMON_ALREADY_HAVE_A_FOLLOWER());
					return false;
				}
			}
		}
		if (!skill.canUseSkill(CastState.CAST_START)) // also initializes effectedList for isIneffectiveHealSkill check down below
			return false;
		if (isIneffectiveHealSkill(effects, skill.getEffectedList())) { // convenience feature: block using ineffective heals
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOTHING_HAPPEN());
			return false;
		}
		return true;
	}

	private static boolean isIneffectiveHealSkill(List<EffectTemplate> effects, List<Creature> effectedList) {
		int hpHealEffects = 0, mpHealEffects = 0;
		for (EffectTemplate template : effects) {
			if (template.getValue() < 0) // negative heal value means damage
				return false;
			if (template instanceof HealEffect || template instanceof HealInstantEffect || template instanceof ProcHealInstantEffect) {
				hpHealEffects++;
			} else if (template instanceof MPHealEffect || template instanceof MPHealInstantEffect || template instanceof ProcMPHealInstantEffect) {
				mpHealEffects++;
			} else {
				return false;
			}
		}
		return (hpHealEffects == 0 || effectedList.stream().allMatch(effected -> effected.getLifeStats().isFullyRestoredHp()))
			&& (mpHealEffects == 0 || effectedList.stream().allMatch(effected -> effected.getLifeStats().isFullyRestoredMp()));
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem, Object... params) {
		Skill skill = SkillEngine.getInstance().getSkill(player, skillid, level, player.getTarget(), parentItem.getItemTemplate());
		if (skill != null) {
			player.getController().cancelUseItem();
			skill.setItemObjectId(parentItem.getObjectId());
			skill.useSkill();
		}
	}

	public int getMapId() {
		return mapid;
	}

}
