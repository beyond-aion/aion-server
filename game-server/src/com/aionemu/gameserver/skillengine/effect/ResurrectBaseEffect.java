package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.custom.pvpmap.PvpMapService;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.TaskId;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.player.PlayerReviveService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ResurrectBaseEffect")
public class ResurrectBaseEffect extends ResurrectEffect {

	@Override
	public void calculate(Effect effect) {
		calculate(effect, null, null);
	}

	@Override
	public void applyEffect(Effect effect) {
		effect.addToEffectedController();
	}

	@Override
	public void endEffect(Effect effect) {
		Creature effected = effect.getEffected();
		if (effected.isDead() && effected instanceof Player player && !PvpMapService.getInstance().isOnPvPMap(player)) {
			player.getController().addTask(TaskId.TELEPORT, ThreadPoolManager.getInstance().schedule(() -> {
				PacketSendUtility.sendPacket(player, new SM_EMOTION(player, EmotionType.RESURRECT));
				if (player.isInInstance())
					PlayerReviveService.instanceRevive(player, skillId);
				else if (player.getKisk() != null)
					PlayerReviveService.kiskRevive(player, skillId);
				else
					PlayerReviveService.bindRevive(player, skillId);
			}, 2500));
		}
	}
}
