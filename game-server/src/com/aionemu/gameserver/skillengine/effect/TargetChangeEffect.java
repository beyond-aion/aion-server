package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_SELECTED;
import com.aionemu.gameserver.network.aion.serverpackets.SM_TARGET_UPDATE;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Bobobear
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "TargetChangeEffect")
public class TargetChangeEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		// TODO Auto-generated method stub
		switch (position) {
			case 3: // remove all targets
				// is not retail
				Creature effected = effect.getEffected();
				if (effected instanceof Player) {
					Player player = (Player) effected;
					player.setTarget(null);
					PacketSendUtility.sendPacket(player, new SM_TARGET_SELECTED(player));
					PacketSendUtility.broadcastPacket(player, new SM_TARGET_UPDATE(player));
				}
				break;
		}
	}
}
