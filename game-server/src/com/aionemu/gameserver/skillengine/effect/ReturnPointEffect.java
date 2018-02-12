package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.item.ItemTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author ATracer
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "ReturnPointEffect")
public class ReturnPointEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		Player player = (Player) effect.getEffector();
		if (player.isInState(CreatureState.RESTING)) {
			player.unsetState(CreatureState.RESTING);
			PacketSendUtility.broadcastPacket(player,
				new SM_EMOTION(player, EmotionType.STAND, 0, player.getX(), player.getY(), player.getZ(), player.getHeading(), getTargetObjectId(player)),
				true);
		}
		ItemTemplate itemTemplate = effect.getItemTemplate();
		int worldId = itemTemplate.getReturnWorldId();
		String pointAlias = itemTemplate.getReturnAlias();
		TeleportService.useTeleportScroll(((Player) effect.getEffector()), pointAlias, worldId);
	}

	@Override
	public void calculate(Effect effect) {
		ItemTemplate itemTemplate = effect.getItemTemplate();
		if (itemTemplate != null)
			effect.addSuccessEffect(this);
	}

	private final int getTargetObjectId(Player player) {
		return player.getTarget() == null ? 0 : player.getTarget().getObjectId();
	}

}
