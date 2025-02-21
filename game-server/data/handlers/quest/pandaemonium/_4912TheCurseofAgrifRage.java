package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author sky123
 */
public class _4912TheCurseofAgrifRage extends AbstractQuestHandler {

	public _4912TheCurseofAgrifRage() {
		super(4912);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798317).addOnQuestStart(questId);
		qe.registerQuestNpc(798317).addOnTalkEvent(questId);
		qe.registerQuestNpc(700514).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 798317) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env, 182207083, 1);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 700514) {
				switch (dialogActionId) {
					case USE_OBJECT:
						if (removeQuestItem(env, 182207083, 1))
							spawn(215385, player.getWorldMapInstance(), (float) 875.9357, (float) 537.4735, (float) 329.7143, (byte) 0);
						return true;
				}
			} else if (targetId == 798317) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case CHECK_USER_HAS_QUEST_ITEM:
						if (player.getInventory().getItemCountByItemId(182207084) >= 1)
							return defaultCloseDialog(env, 0, 0, true, true, 0, 0, 182207084, 1);
						else
							return sendQuestDialog(env, 10001);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
