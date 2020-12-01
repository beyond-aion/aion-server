package quest.pandaemonium;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Put the Inactivated Surkana inside the Balaur Material Converter (730212). Talk with Chopirunerk (798358).
 * 
 * @author Bobobear
 */
public class _4920MakingTheActivatedSurkana extends AbstractQuestHandler {

	public _4920MakingTheActivatedSurkana() {
		super(4920);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798358).addOnQuestStart(questId); // Chopirunerk
		qe.registerQuestNpc(798358).addOnTalkEvent(questId); // Chopirunerk
		qe.registerQuestNpc(730212).addOnTalkEvent(questId); // Balaur Material Converter
		qe.registerQuestItem(182207100, questId);
		qe.registerQuestItem(182207101, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		if (qs == null || qs.isStartable()) {
			if (targetId == 798358) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env, 182207100, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 730212: // Balaur Material Converter
					switch (dialogActionId) {
						case USE_OBJECT:
							if ((var == 0) && player.getInventory().getItemCountByItemId(182207100) > 0) {
								return useQuestObject(env, 0, 1, true, 0, 182207101, 1, 182207100, 1, 0, false);
							}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798358) { // Chopirunerk
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
