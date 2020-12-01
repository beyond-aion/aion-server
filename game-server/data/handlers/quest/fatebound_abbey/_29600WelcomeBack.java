package quest.fatebound_abbey;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Pad
 */
public class _29600WelcomeBack extends AbstractQuestHandler {

	private static final int npcId = 804662; // Melanka
	private static final int itemId = 164000336; // Abbey Return Stone

	public _29600WelcomeBack() {
		super(29600);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		qe.registerOnGetItem(itemId, questId); // quest acquisition linked to abbey return stone acquisition
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == npcId) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case SETPRO1:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return defaultCloseDialog(env, 0, 0, true, true);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcId) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		return QuestService.startQuest(env);
	}

}
