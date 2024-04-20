package quest.poeta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author MrPoke, Majka
 */
public class _1004NeutralizingOdium extends AbstractQuestHandler {

	public _1004NeutralizingOdium() {
		super(1004);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(203082).addOnTalkEvent(questId); // Tula
		qe.registerQuestNpc(700030).addOnTalkEvent(questId); // The Cauldron
		qe.registerQuestNpc(790001).addOnTalkEvent(questId); // Pernos
		qe.registerQuestNpc(203067).addOnTalkEvent(questId); // Kalio
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203082) { // Tula
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						else if (var == 5)
							return sendQuestDialog(env, 2034);
						return false;
					case SELECT1_1_1:
						if (var == 0)
							playQuestMovie(env, 19);
						return false;
					case SETPRO1:
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						sendQuestSelectionDialog(env);
						return true;
					case SETPRO3:
						if (var == 5) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							sendQuestSelectionDialog(env);
							return true;
						}
				}
			} else if (targetId == 700030 && var == 1 || var == 4) { // The Cauldron
				switch (dialogActionId) {
					case USE_OBJECT:
						if (var == 1) {
							if (giveQuestItem(env, 182200005, 1))
								qs.setQuestVarById(0, var + 1);
						} else if (var == 4) {
							qs.setQuestVarById(0, var + 1);
							removeQuestItem(env, 182200005, 1);
						}
						updateQuestStatus(env);
						return false;
				}
			} else if (targetId == 790001) { // Pernos
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 2)
							return sendQuestDialog(env, 1352);
						else if (var == 3)
							return sendQuestDialog(env, 1693);
						else if (var == 11)
							return sendQuestDialog(env, 1694);
						return false;
					case SETPRO2:
						if (var == 2) {
							qs.setQuestVarById(0, var + 1);
							updateQuestStatus(env);
							sendQuestSelectionDialog(env);
							return true;
						}
						return false;
					case SETPRO3:
						if (var == 11) {
							if (!giveQuestItem(env, 182200006, 1))
								return true;
							qs.setQuestVarById(0, 4);
							updateQuestStatus(env);
							removeQuestItem(env, 182200006, 1);
							sendQuestSelectionDialog(env);
							return true;
						}
						return false;
					case CHECK_USER_HAS_QUEST_ITEM:
						if (QuestService.collectItemCheck(env, true)) {
							qs.setQuestVarById(0, 11);
							updateQuestStatus(env);
							return sendQuestDialog(env, 1694);
						} else
							return sendQuestDialog(env, 1779);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203067) // Kalio
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 1100);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 1100);
	}
}
