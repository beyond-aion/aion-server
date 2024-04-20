package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi, vlog
 */
public class _2952WinningVindachinerksFavor extends AbstractQuestHandler {

	public _2952WinningVindachinerksFavor() {
		super(2952);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(279006).addOnQuestStart(questId); // Garkbinerk
		qe.registerQuestNpc(279006).addOnTalkEvent(questId); // Garkbinerk
		qe.registerQuestNpc(279016).addOnTalkEvent(questId); // Vindachinerk
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 279006) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVars().getQuestVars();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 279016: // Vindachinerk
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 2375);
							}
							return false;
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 0, 0, true, 5, 2716); // reward
						case FINISH_DIALOG:
							return defaultCloseDialog(env, 0, 0);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD && targetId == 279016) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
