package quest.siels_spear;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Cheatkiller
 *
 */
public class _41469TheRealSielSpears extends QuestHandler {
	private final static int questId = 41469;
	
	public _41469TheRealSielSpears(){
		super(questId);
	}
		@Override
		public void register() {
			qe.registerQuestNpc(205579).addOnQuestStart(questId);
			qe.registerQuestNpc(205579).addOnTalkEvent(questId);
		}
		
		@Override
		public boolean onDialogEvent(QuestEnv env) {
			Player player = env.getPlayer();
			int targetId = env.getTargetId();
			DialogAction dialog = env.getDialog();
			QuestState qs = player.getQuestStateList().getQuestState(questId);
		
			if (qs == null || qs.getStatus() == QuestStatus.NONE) {
				if (targetId == 205579) {
					if (dialog == DialogAction.QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
					}
					else {
						return sendQuestStartDialog(env);
					}
				}
			}
			else if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				switch (targetId) {
					case 205579: { 
						switch (dialog) {
							case QUEST_SELECT: {
								if (var == 0) {
									return sendQuestDialog(env, 2375);
								}
							}
							case SELECT_QUEST_REWARD: {
								return defaultCloseDialog(env, 0, 1, true, true);
							}
						}
						break;
					}
				}
			}
			else if (qs.getStatus() == QuestStatus.REWARD) {
				if (targetId == 205579) {
					if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
						return sendQuestDialog(env, 5);
					else
						return sendQuestEndDialog(env);
				}
			}
			return false;
		}

	}
