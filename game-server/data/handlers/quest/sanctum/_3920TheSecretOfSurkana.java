package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Put the Inactivated Surkana inside the Balaur Material Converter (730212). Talk with Shoshinerk (798357).
 * 
 * @author Bobobear
 */
public class _3920TheSecretOfSurkana extends AbstractQuestHandler {

	public _3920TheSecretOfSurkana() {
		super(3920);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798357).addOnQuestStart(questId); // Shoshinerk
		qe.registerQuestNpc(798357).addOnTalkEvent(questId); // Shoshinerk
		qe.registerQuestNpc(730212).addOnTalkEvent(questId); // Balaur Material Converter
		qe.registerQuestItem(182206073, questId);
		qe.registerQuestItem(182206074, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();
		if (qs == null || qs.isStartable()) {
			if (targetId == 798357) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env, 182206073, 1);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 730212: // Balaur Material Converter
					switch (dialogActionId) {
						case USE_OBJECT:
							if ((var == 0) && player.getInventory().getItemCountByItemId(182206073) > 0) {
								return useQuestObject(env, 0, 1, true, 0, 182206074, 1, 182206073, 1, 0, false);
							}
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798357) { // Shoshinerk
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
