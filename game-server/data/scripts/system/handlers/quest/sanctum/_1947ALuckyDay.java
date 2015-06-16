package quest.sanctum;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Gigi, kale
 */
public class _1947ALuckyDay extends QuestHandler {

	private final static int questId = 1947;

	public _1947ALuckyDay() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203795).addOnQuestStart(questId);
		qe.registerQuestNpc(203795).addOnTalkEvent(questId);// Demodocos
		qe.registerQuestNpc(798012).addOnTalkEvent(questId);// Yiehmonerk
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() == QuestStatus.NONE)// TODO: If cube size is bigger than 5
		{
			switch (env.getDialog()) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 1011);
				case ASK_QUEST_ACCEPT:
					return sendQuestDialog(env, 4);
				case QUEST_ACCEPT_1:
					env.setQuestId(questId);
					QuestService.startQuest(env);
					return sendQuestDialog(env, 1003);
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVars().getQuestVars();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798012:// Yiehmonerk
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 2375);
						case SELECT_QUEST_REWARD:
							if (var == 0) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 5);
							}
					}
					break;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD && targetId == 798012)
			return sendQuestEndDialog(env);
		return false;
	}
}
