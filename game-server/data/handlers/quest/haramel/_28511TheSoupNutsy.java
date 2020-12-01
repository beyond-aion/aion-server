package quest.haramel;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author vlog
 */
public class _28511TheSoupNutsy extends AbstractQuestHandler {

	public _28511TheSoupNutsy() {
		super(28511);
	}

	@Override
	public void register() {
		int[] npcs = { 799522, 700954, 730359, 798031 };
		qe.registerQuestNpc(799522).addOnQuestStart(questId);
		qe.registerOnGetItem(182212023, questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 799522) { // Moorilerk
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 700954: { // Well-Dried Ginseng
					return true; // loot
				}
				case 730359: // Huge Cauldron
					switch (dialogActionId) {
						case USE_OBJECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 0, 0, false, 1352, 10001);
						case SETPRO2:
							giveQuestItem(env, 182212023, 1);
							return closeDialogWindow(env);
						case FINISH_DIALOG:
							return closeDialogWindow(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798031) { // Chagarinerk
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		defaultOnGetItemEvent(env, 0, 1, false); // 1
		changeQuestStep(env, 1, 1, true); // reward
		return true;
	}
}
