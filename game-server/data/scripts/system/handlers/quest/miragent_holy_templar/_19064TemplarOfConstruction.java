package quest.miragent_holy_templar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Cheatkiller
 */
public class _19064TemplarOfConstruction extends AbstractQuestHandler {

	public _19064TemplarOfConstruction() {
		super(19064);
	}

	@Override
	public void register() {
		int[] npcs = { 203701, 798450, 203752 };
		qe.registerQuestNpc(203701).addOnQuestStart(questId);
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203701) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798450:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 203752:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1 && player.getInventory().getItemCountByItemId(182213237) >= 1
								&& player.getInventory().getItemCountByItemId(186000081) >= 1) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						case CHECK_USER_HAS_QUEST_ITEM_SIMPLE:
							if (player.getInventory().getItemCountByItemId(182213237) >= 1 && player.getInventory().getItemCountByItemId(186000081) >= 1) {
								removeQuestItem(env, 182213237, 1);
								removeQuestItem(env, 186000081, 1);
								return defaultCloseDialog(env, 1, 1, true, false);
							}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203701) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 5);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
