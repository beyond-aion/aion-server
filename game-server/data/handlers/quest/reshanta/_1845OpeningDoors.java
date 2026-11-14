package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author MrPoke, Nephis
 */
public class _1845OpeningDoors extends AbstractQuestHandler {

	public _1845OpeningDoors() {
		super(1845);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(278591).addOnTalkEvent(questId);
		qe.registerQuestNpc(278624).addOnTalkEvent(questId);
		qe.registerQuestNpc(798316).addOnTalkEvent(questId);
		qe.registerQuestNpc(204390).addOnTalkEvent(questId);
		qe.registerQuestItem(182202181, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = env.getVisibleObject() instanceof Npc npc ? npc.getNpcId() : 0;
		if (targetId == 0) {
			if (env.getDialogActionId() == QUEST_ACCEPT_1 && (qs == null || qs.isStartable())) {
				QuestService.startQuest(env);
				return closeDialogWindow(env);
			}
		} else if (targetId == 278591) { // Deidamia
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialogActionId() == SETPRO1)
					return defaultCloseDialog(env, 0, 1);
			}
		} else if (targetId == 278624) { // Lycian
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialogActionId() == SETPRO2)
					return defaultCloseDialog(env, 1, 2);
			}
		} else if (targetId == 798316) { // Anusis
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
					removeQuestItem(env, 182202181, 1);
					return defaultCloseDialog(env, 2, 2, true, true);
				}
			}
			return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		int id = item.getItemTemplate().getTemplateId();

		if (id != 182202181)
			return HandlerResult.UNKNOWN;
		sendQuestDialog(env, 4);
		return HandlerResult.SUCCESS;
	}
}
