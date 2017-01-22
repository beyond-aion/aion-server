package quest.nightmare_circus;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _80341EventRequestfromPucas extends QuestHandler {

	private static final int questId = 80341;

	public _80341EventRequestfromPucas() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(831717).addOnQuestStart(questId);
		qe.registerQuestNpc(831716).addOnQuestStart(questId);
		qe.registerQuestNpc(831715).addOnQuestStart(questId);
		qe.registerQuestNpc(831714).addOnQuestStart(questId);
		qe.registerQuestNpc(831713).addOnQuestStart(questId);
		qe.registerQuestNpc(831712).addOnQuestStart(questId);
		qe.registerQuestNpc(831711).addOnQuestStart(questId);
		qe.registerQuestNpc(831710).addOnQuestStart(questId);
		qe.registerQuestNpc(831709).addOnQuestStart(questId);
		qe.registerQuestNpc(831717).addOnTalkEvent(questId);
		qe.registerQuestNpc(831716).addOnTalkEvent(questId);
		qe.registerQuestNpc(831715).addOnTalkEvent(questId);
		qe.registerQuestNpc(831714).addOnTalkEvent(questId);
		qe.registerQuestNpc(831713).addOnTalkEvent(questId);
		qe.registerQuestNpc(831712).addOnTalkEvent(questId);
		qe.registerQuestNpc(831711).addOnTalkEvent(questId);
		qe.registerQuestNpc(831710).addOnTalkEvent(questId);
		qe.registerQuestNpc(831709).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 831717 || targetId == 831716 || targetId == 831715 || targetId == 831714 || targetId == 831713 || targetId == 831712
				|| targetId == 831711 || targetId == 831710 || targetId == 831709) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;
		else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 831717:
				case 831716:
				case 831715:
				case 831714:
				case 831713:
				case 831712:
				case 831711:
				case 831710:
				case 831709:
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						}
						case SELECT_QUEST_REWARD: {
							changeQuestStep(env, 0, 0, true);
							return sendQuestDialog(env, 5);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 831717 || targetId == 831716 || targetId == 831715 || targetId == 831714 || targetId == 831713 || targetId == 831712
				|| targetId == 831711 || targetId == 831710 || targetId == 831709) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
