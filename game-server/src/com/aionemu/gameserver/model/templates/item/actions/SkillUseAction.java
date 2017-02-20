package com.aionemu.gameserver.model.templates.item.actions;

import java.util.List;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.stats.container.PlayerLifeStats;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
import com.aionemu.gameserver.skillengine.effect.HealEffect;
import com.aionemu.gameserver.skillengine.effect.HealInstantEffect;
import com.aionemu.gameserver.skillengine.effect.MPHealEffect;
import com.aionemu.gameserver.skillengine.effect.MPHealInstantEffect;
import com.aionemu.gameserver.skillengine.effect.ProcHealInstantEffect;
import com.aionemu.gameserver.skillengine.effect.ProcMPHealInstantEffect;
import com.aionemu.gameserver.skillengine.effect.SummonEffect;
import com.aionemu.gameserver.skillengine.effect.TransformEffect;
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
	private Integer mapid;

	/**
	 * Gets the value of the skillid property.
	 */
	public int getSkillid() {
		return skillid;
	}

	/**
	 * Gets the value of the level property.
	 */
	public int getLevel() {
		return level;
	}

	@Override
	public boolean canAct(Player player, Item parentItem, Item targetItem) {
		Skill skill = SkillEngine.getInstance().getSkill(player, skillid, level, player.getTarget(), parentItem.getItemTemplate(), false);
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
			PlayerLifeStats lifeStats = player.getLifeStats();
			if (lifeStats.isFullyRestoredHp() || lifeStats.isFullyRestoredMp()) { // convenience feature: block using ineffective heals
				int hpHealEffects = 0, mpHealEffects = 0;
				for (EffectTemplate template : effects) {
					if (template instanceof HealEffect || template instanceof HealInstantEffect || template instanceof ProcHealInstantEffect) {
						hpHealEffects++;
					} else if (template instanceof MPHealEffect || template instanceof MPHealInstantEffect || template instanceof ProcMPHealInstantEffect) {
						mpHealEffects++;
					}
				}
				if (hpHealEffects + mpHealEffects == effects.size() && !(!lifeStats.isFullyRestoredHp() && hpHealEffects > 0)
					&& !(!lifeStats.isFullyRestoredMp() && mpHealEffects > 0)) {
					PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_NOTHING_HAPPEN());
					return false;
				}
			}
		}
		return skill.canUseSkill(CastState.CAST_START);
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem) {
		Skill skill = SkillEngine.getInstance().getSkill(player, skillid, level, player.getTarget(), parentItem.getItemTemplate(), false);
		if (skill != null) {
			player.getController().cancelUseItem();
			skill.setItemObjectId(parentItem.getObjectId());
			skill.useSkill();
		}
	}

	public int getMapid() {
		if (mapid == null)
			return 0;
		return mapid;
	}

}
