package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _21081A_Helping_Hand extends AbstractQuestHandler {

	public _21081A_Helping_Hand() {
		super(21081);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799225).addOnQuestStart(questId); // Richelle
		qe.registerQuestNpc(799225).addOnTalkEvent(questId); // Richelle
		qe.registerQuestNpc(799332).addOnTalkEvent(questId); // Agovard
		qe.registerQuestNpc(799217).addOnTalkEvent(questId); // Renato
		qe.registerQuestNpc(799202).addOnTalkEvent(questId); // Ipses
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799225) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					default: {
						return sendQuestStartDialog(env, 182214017, 1);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 799332: // Brontes
				{
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1353);
						case SELECT2_1:
							return sendQuestDialog(env, 1353);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
					}
					return false;
				}
				case 799217: // Pilipides
				{
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1693);
						case SELECT3_1:
							return sendQuestDialog(env, 1694);
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
					return false;
				}
				case 799202: // Drenia
				{
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 2375);
						case SELECT_QUEST_REWARD:
							return defaultCloseDialog(env, 2, 3, true, true);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799202) { // Drenia
				switch (env.getDialogActionId()) {
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
