package com.aionemu.gameserver.skillengine.effect;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlType;

import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.gameobjects.player.RequestResponseHandler;
import com.aionemu.gameserver.network.aion.serverpackets.SM_RECALLED_BY_OTHER;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.model.Effect;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneInstance;

/**
 * @author Bio, Sippolo, SVDNESS
 */
@XmlAccessorType(XmlAccessType.FIELD)
@XmlType(name = "RecallInstantEffect")
public class RecallInstantEffect extends EffectTemplate {

	@Override
	public void applyEffect(Effect effect) {
		Creature effector = effect.getEffector();
		Player effected = (Player) effect.getEffected();
		int worldId = effect.getWorldId();
		int instanceId = effect.getInstanceId();
		float locationX = effect.getSkill().getX();
		float locationY = effect.getSkill().getY();
		float locationZ = effect.getSkill().getZ();
		byte locationH = effect.getSkill().getH();
		RequestResponseHandler<Creature> rrh = new RequestResponseHandler<>(effector) {
			@Override
			public void acceptRequest(Creature effector, Player effected) {
				TeleportService.teleportTo(effected, worldId, instanceId, locationX, locationY, locationZ, locationH);
			}

			@Override
			public void handle(Player responder, int answer) {
				switch (answer) {
					case 0 -> acceptRequest(effector, responder); //Accept.
					case 1 -> { //Refuse.
						//%0 declined your summoning.
						PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_MSG_Recall_Rejected_EFFECT(responder.getName()));
						//You declined %0's summoning.
						PacketSendUtility.sendPacket(responder, SM_SYSTEM_MESSAGE.STR_MSG_Recall_Reject_EFFECT(effector.getName()));
					}
					case 2 -> //Time-out.
						//Summoning of %0 is cancelled as the confirmation stand-by time has been exceeded.
						PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_MSG_Recall_DONOT_ACCEPT_EFFECT(responder.getName()));
					default -> {}
				}
			}
		};
		if (effected.getResponseRequester().putRequest(SM_RECALLED_BY_OTHER.RECALL_REQUEST_ID, rrh)) {
			PacketSendUtility.sendPacket(effected, new SM_RECALLED_BY_OTHER(effector.getName(), effect.getSkillId(), 30));
		} else {
			//You cannot summon %0 as you are already under the same effect.
			PacketSendUtility.sendPacket((Player) effector, SM_SYSTEM_MESSAGE.STR_MSG_Recall_DUPLICATE_EFFECT(effected.getName()));
		}
	}

	@Override
	public void calculate(Effect effect) {
		Creature effector = effect.getEffector();
		if (!(effect.getEffected() instanceof Player effected)) {
			return;
		}
		if (effected.getController().isInCombat()) {
			return;
		}
		if (effector.getWorldId() != effected.getWorldId()) {
			return;
		}
		if (effector.isEnemy(effected)) {
			return;
		}
		if (!canRecallTo(effector)) {
			return;
		}
		effect.getSkill().setTargetPosition(effector.getX(), effector.getY(), effector.getZ(), effector.getHeading());
		effect.addSuccessEffect(this);
	}
	
	//Single check for recall restrictions in the destination zone and world. Used before and after the cast.
	public static boolean canRecallTo(Creature effector) {
		for (ZoneInstance zone : effector.findZones()) {
			if (!zone.canRecall()) {
				return false;
			}
		}
		return effector.getPosition().getWorldMapInstance().getParent().canRecall();
	}
}