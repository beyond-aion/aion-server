package quest.theobomos;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

public class _3095ADecisiveClue extends AbstractQuestHandler {

	public _3095ADecisiveClue() {
		super(3095);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(700422).addOnQuestStart(questId); // Faded Book
		qe.registerQuestNpc(700422).addOnTalkEvent(questId);
		qe.registerQuestNpc(798225).addOnTalkEvent(questId);
		qe.registerQuestNpc(203898).addOnTalkEvent(questId);
		qe.registerQuestItem(182208053, questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 0) {
				if (env.getDialogActionId() == QUEST_ACCEPT_1) {
					QuestService.startQuest(env);
					return closeDialogWindow(env);
				}
			} else if (targetId == 700422) {
				return giveQuestItem(env, 182208053, 1);
			}
		}

		switch (targetId) {
			case 798225:
				if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 0) {
					if (env.getDialogActionId() == QUEST_SELECT)
						return sendQuestDialog(env, 1352);
					else if (env.getDialogActionId() == SETPRO1) {
						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
						removeQuestItem(env, 182208053, 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					} else
						return sendQuestStartDialog(env);
				} else if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 2) {
					if (env.getDialogActionId() == QUEST_SELECT)
						return sendQuestDialog(env, 2375);
					else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 5);
					} else
						return sendQuestStartDialog(env);
				} else if (qs != null && qs.getStatus() == QuestStatus.REWARD)
					return sendQuestEndDialog(env);
				return false;
			case 203898:
				if (qs != null && qs.getStatus() == QuestStatus.START && qs.getQuestVarById(0) == 1) {
					if (env.getDialogActionId() == QUEST_SELECT)
						return sendQuestDialog(env, 1693);
					else if (env.getDialogActionId() == SETPRO2) {
						qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					} else
						return sendQuestStartDialog(env);
				}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			return HandlerResult.fromBoolean(sendQuestDialog(env, 4));
		}
		return HandlerResult.FAILED;
	}
}
