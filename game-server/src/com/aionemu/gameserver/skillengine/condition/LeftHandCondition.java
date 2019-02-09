package com.aionemu.gameserver.skillengine.condition;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.item.LeftHandSlot;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.skillengine.model.Skill;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Cheatkiller
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "LeftHandCondition")
public class LeftHandCondition extends Condition {

	@XmlAttribute(name = "type")
	private LeftHandSlot type;

	@Override
	public boolean validate(Skill env) {
		if (env.getEffector() instanceof Player) {
			Player player = (Player) env.getEffector();
			switch (type) {
				case DUAL:
					Item offHandWeapon = player.getEquipment().getOffHandWeapon();
					Item mainHandWeapon;
					if (offHandWeapon != null && offHandWeapon.getItemTemplate().isWeapon()
						|| (mainHandWeapon = player.getEquipment().getMainHandWeapon()) != null && mainHandWeapon.getItemTemplate().isTwoHandWeapon())
						return true;
					else {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_NEED_DUAL_WEAPON());
						return false;
					}
				case SHIELD:
					if (player.getEquipment().isShieldEquipped())
						return true;
					else {
						PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_SKILL_NEED_SHIELD());
						return false;
					}
			}
		}
		return false;
	}
}
