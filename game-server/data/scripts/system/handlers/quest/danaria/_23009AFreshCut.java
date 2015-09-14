package quest.danaria;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;


/**
 * @author Evil_dnk
 *
 */
public class _23009AFreshCut extends QuestHandler {

	private final static int questId = 23009;

	public _23009AFreshCut() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(801108).addOnQuestStart(questId);
		qe.registerQuestNpc(801108).addOnTalkEvent(questId);
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        DialogAction dialog = env.getDialog();

        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 801108) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 1011);
                }
                else {
                    return sendQuestStartDialog(env);
                }
            }
        }
        else if (qs.getStatus() == QuestStatus.START) {
            if (targetId == 801108)
            {
             if (dialog == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 2375);
            }
            if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM_SIMPLE) {
                return checkQuestItems(env, 0, 0, false, 10002, 10001);
                }
                if (dialog == DialogAction.SELECT_QUEST_REWARD) {
                    changeQuestStep(env, 0, 1, true);
                    return sendQuestEndDialog(env);
                }
             }
        }
        else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 801108) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

}