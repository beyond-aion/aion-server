package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author MrPoke remod By Nephis
 * @reworked vlog
 */
public class _2493BringingUpTayga extends QuestHandler {

	private final static int questId = 2493;

	public _2493BringingUpTayga() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204325).addOnQuestStart(questId);
		qe.registerOnLogOut(questId);
		qe.registerQuestNpc(204325).addOnTalkEvent(questId);
		qe.registerQuestNpc(204435).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204325) { // Ipoderr
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204435) { // Purra? 1st Spot
				if (dialog == DialogAction.QUEST_SELECT) {
					if (var == 0) {
						return sendQuestDialog(env, 1011);
					}
				} else if (dialog == DialogAction.SET_SUCCEED) {
					final Npc npc = (Npc) env.getVisibleObject();
					NpcActions.delete(npc, true);
					changeQuestStep(env, 0, 0, true); // reward
					return closeDialogWindow(env);
				}
			}
			else if (targetId == 204436 || targetId == 204437 || targetId == 204438) { // Purra? other Spots
				if (dialog == DialogAction.QUEST_SELECT) {
					if (var == 0) {
						return sendQuestDialog(env, 1353);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204325) { // Ipoderr
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
