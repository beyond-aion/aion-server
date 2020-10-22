package com.aionemu.gameserver.ai.handler;

import com.aionemu.gameserver.ai.AIState;
import com.aionemu.gameserver.ai.AISubState;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.DialogService;
import com.aionemu.gameserver.services.TownService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author ATracer
 */
public class TalkEventHandler {

	public static void onTalk(NpcAI npcAI, Creature creature) {
		onSimpleTalk(npcAI, creature);

		if (creature instanceof Player player) {
			if (QuestEngine.getInstance().onDialog(new QuestEnv(npcAI.getOwner(), player, 0, DialogAction.USE_OBJECT)))
				return;
			// only player villagers can use villager npcs in oriel/pernon
			switch (npcAI.getOwner().getObjectTemplate().getTitleId()) {
				case 462877:
					int playerTownId = TownService.getInstance().getTownResidence(player);
					int currentTownId = TownService.getInstance().getTownIdByPosition(player);
					if (playerTownId != currentTownId) {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npcAI.getOwner().getObjectId(), 44));
					} else {
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npcAI.getOwner().getObjectId(), 10));
					}
					return;
				default:
					int dialogPageId = DialogService.isInteractionAllowed(player, npcAI.getOwner()) ? 10 : 1011;
					PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(npcAI.getOwner().getObjectId(), dialogPageId));
					break;
			}
		}

	}

	public static void onSimpleTalk(NpcAI npcAI, Creature creature) {
		if (npcAI.getOwner().getObjectTemplate().isDialogNpc()) {
			npcAI.setSubStateIfNot(AISubState.TALK);
			npcAI.getOwner().setTarget(creature);
		}
	}

	public static void onFinishTalk(NpcAI npcAI, Creature creature) {
		Npc owner = npcAI.getOwner();
		if (owner.isTargeting(creature.getObjectId())) {
			if (npcAI.getState() == AIState.FOLLOWING) {
				npcAI.think();
			} else {
				owner.setTarget(null);
				ThreadPoolManager.getInstance().schedule(() -> {
					if (npcAI.getOwner().getTarget() == null)
						npcAI.think();
				}, 750);
			}
		}
	}

}
