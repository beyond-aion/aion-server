package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author zhkchi
 */
public class _1917ALingeringMystery extends AbstractQuestHandler {

	public _1917ALingeringMystery() {
		super(1917);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203835).addOnQuestStart(questId);
		qe.registerQuestNpc(203835).addOnTalkEvent(questId);
		qe.registerQuestNpc(203075).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (sendQuestNoneDialog(env, 203835))
			return true;

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203075) { // Namus
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 0)
						return sendQuestDialog(env, 1352);
				} else if (env.getDialogActionId() == SETPRO1) {
					return defaultCloseDialog(env, 0, 1, false, false);
				}
			} else if (targetId == 203835) { // Seirenia
				if (env.getDialogActionId() == QUEST_SELECT) {
					if (qs.getQuestVarById(0) == 1)
						return sendQuestDialog(env, 1693);
				} else {
					Integer rewardGroup = switch (env.getDialogActionId()) {
						case SETPRO1 -> 0;
						case SETPRO2 -> 1;
						case SETPRO3 -> 2;
						default -> null;
					};
					if (rewardGroup != null) {
						qs.setRewardGroup(rewardGroup);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, DialogPage.getRewardPageByIndex(qs.getRewardGroup()).id());
					}
				}
			}
		}
		return sendQuestRewardDialog(env, 203835, 0);
	}
}
