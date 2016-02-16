package quest.crafting;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 * @modified Pad
 */
public class _19001ExpertEssencetappingExpert extends QuestHandler {

	private final static int questId = 19001;

	public _19001ExpertEssencetappingExpert() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203780).addOnQuestStart(questId);
		qe.registerQuestNpc(203780).addOnTalkEvent(questId);
		qe.registerQuestNpc(203700).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 203780) {
				switch (dialog) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					case ASK_QUEST_ACCEPT:
						return sendQuestDialog(env, 4);
					case QUEST_ACCEPT_1:
					case QUEST_ACCEPT_SIMPLE:
						return sendQuestStartDialog(env, 182206127, 1);
					case QUEST_REFUSE_1:
					case QUEST_REFUSE_SIMPLE:
						return sendQuestDialog(env, 1004);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203700: {
					switch (dialog) {
						case QUEST_SELECT:
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 2375);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203700) {
				if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM)
					return sendQuestDialog(env, 5);
				else {
					player.getSkillList().addSkill(player, 30002, 400);
					removeQuestItem(env, 182206127, 1);
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
