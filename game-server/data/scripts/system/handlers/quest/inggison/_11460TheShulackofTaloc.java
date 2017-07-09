package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _11460TheShulackofTaloc extends AbstractQuestHandler {

	public _11460TheShulackofTaloc() {
		super(11460);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798954).addOnQuestStart(questId);
		qe.registerQuestNpc(798954).addOnTalkEvent(questId);
		qe.registerQuestNpc(799502).addOnTalkEvent(questId);
		qe.registerQuestNpc(798985).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798954) { // Tialla
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 799502: // Dorkin
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1, 182209509, 1, 0, 0); // 1
					}
					break;
				case 798985: // Seikin
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						case SELECT_QUEST_REWARD:
							if (removeQuestItem(env, 182209509, 1))
								changeQuestStep(env, 1, 1, true); // reward
							return sendQuestDialog(env, 5);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798985) { // Seikin
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
