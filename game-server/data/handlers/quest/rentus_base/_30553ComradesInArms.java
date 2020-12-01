package quest.rentus_base;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Ritsu
 */
public class _30553ComradesInArms extends AbstractQuestHandler {

	public _30553ComradesInArms() {
		super(30553);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205438).addOnQuestStart(questId); // Lition
		qe.registerQuestNpc(205438).addOnTalkEvent(questId);
		qe.registerQuestNpc(701097).addOnTalkEvent(questId);
		qe.registerQuestNpc(799541).addOnTalkEvent(questId); // rodelion

	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();
		if (qs == null || qs.isStartable()) {
			switch (targetId) {
				case 205438:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 4762);
						default:
							return sendQuestStartDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.START) {

			switch (targetId) {
				case 205438:
					switch (dialogActionId) {
						case SELECT_QUEST_REWARD:
							return defaultCloseDialog(env, 1, 1, true, true);
					}
					return false;
				case 701097:
					Npc npc = (Npc) env.getVisibleObject();
					npc.getController().delete();
					return true;
				case 799541:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1011);
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 0, 1, true, false);// reward
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch (targetId) {
				case 205438:

					return sendQuestEndDialog(env);

			}
		}
		return false;
	}
}
