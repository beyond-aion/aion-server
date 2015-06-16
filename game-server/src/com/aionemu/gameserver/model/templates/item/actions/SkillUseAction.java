package com.aionemu.gameserver.model.templates.item.actions;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.DescriptionId;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.effect.EffectTemplate;
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
		Skill skill = SkillEngine.getInstance().getSkill(player, skillid, level, player.getTarget(), parentItem.getItemTemplate());
		if (skill == null)
			return false;
		int nameId = parentItem.getItemTemplate().getNameId();
		// Cant use transform items while already transformed
		if (player.isTransformed()) {
			for (EffectTemplate template : skill.getSkillTemplate().getEffects().getEffects()) {
				if (template instanceof TransformEffect) {
					PacketSendUtility.sendPacket(player,
						SM_SYSTEM_MESSAGE.STR_CANT_USE_ITEM(new DescriptionId(nameId)));
					return false;
				}
			}
		}
		if (player.getSummon() != null) {
			for (EffectTemplate template : skill.getSkillTemplate().getEffects().getEffects()) {
				if (template instanceof SummonEffect) {
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(1300072));
					return false;
				}
			}
		}
		return skill.canUseSkill(CastState.CAST_START);
	}

	@Override
	public void act(Player player, Item parentItem, Item targetItem) {
		Skill skill = SkillEngine.getInstance().getSkill(player, skillid, level, player.getTarget(), parentItem.getItemTemplate());
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
