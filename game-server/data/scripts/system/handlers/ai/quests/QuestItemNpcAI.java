package ai.quests;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.handler.CreatureEventHandler;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.drop.DropService;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.ActionItemNpcAI;

/**
 * @author xTz
 */
@AIName("quest_use_item")
public class QuestItemNpcAI extends ActionItemNpcAI {

	public QuestItemNpcAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		if (!(QuestEngine.getInstance().onCanAct(new QuestEnv(getOwner(), player, 0), getObjectTemplate().getTemplateId(),
			QuestActionType.ACTION_ITEM_USE))) {
			return;
		}
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		QuestEnv env = new QuestEnv(getOwner(), player, 0, DialogAction.USE_OBJECT);
		if (!QuestEngine.getInstance().onDialog(env)) {
			if (getObjectTemplate().isDialogNpc()) // show default dialog
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(getObjectId(), 1011));
			return;
		}

		if (QuestService.getQuestDrop(getNpcId()).isEmpty())
			return;

		List<Player> registeredPlayers = new ArrayList<>();
		if (player.isInGroup()) {
			registeredPlayers = QuestService.getEachDropMembersGroup(player.getPlayerGroup(), getNpcId(), env.getQuestId());
			if (registeredPlayers.isEmpty()) {
				registeredPlayers.add(player);
			}
		} else if (player.isInAlliance()) {
			registeredPlayers = QuestService.getEachDropMembersAlliance(player.getPlayerAlliance(), getNpcId(), env.getQuestId());
			if (registeredPlayers.isEmpty()) {
				registeredPlayers.add(player);
			}
		} else {
			registeredPlayers.add(player);
		}
		AIActions.registerDrop(this, player, registeredPlayers);
		AIActions.die(this, player);
		DropService.getInstance().requestDropList(player, getObjectId());
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		CreatureEventHandler.onCreatureSee(this, creature);
	}
}
