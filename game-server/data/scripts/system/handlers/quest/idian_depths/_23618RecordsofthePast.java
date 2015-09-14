package quest.idian_depths;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Tibald
 */
public class _23618RecordsofthePast extends QuestHandler {

    private final static int questId = 23618;

    public _23618RecordsofthePast() {
        super(questId);
    }

    @Override
		public void register() {
        qe.registerQuestNpc(801547).addOnQuestStart(questId);
        qe.registerQuestNpc(801547).addOnTalkEvent(questId);
        qe.registerQuestNpc(730823).addOnTalkEvent(questId);
        qe.registerQuestNpc(730824).addOnTalkEvent(questId);

    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        DialogAction dialog = env.getDialog();

        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 801547) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 1011);
                }
                else {
                    return sendQuestStartDialog(env);
                }
            }
        }
        else if (qs.getStatus() == QuestStatus.START) {
            if (targetId == 730823) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    if (qs.getQuestVarById(0) == 0){
                        return sendQuestDialog(env, 1352);
                    }
                }
                else if (dialog == DialogAction.SETPRO1) {
                    changeQuestStep(env, 0, 1, false);
                    return closeDialogWindow(env);
                }
            }
           else if (targetId == 730824) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    if (qs.getQuestVarById(0) == 1){
                        return sendQuestDialog(env, 1693);
                    }
                }
                else if (dialog == DialogAction.SETPRO2) {
                    changeQuestStep(env, 1, 2, false);
                    return closeDialogWindow(env);
                }
            }
            else if (targetId == 801547) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    if (qs.getQuestVarById(0) == 2){
                        return sendQuestDialog(env, 2375);
                    }
                }
                else if (dialog == DialogAction.SELECT_QUEST_REWARD) {
                    changeQuestStep(env, 2, 3, true);
                    return sendQuestDialog(env, 5);
                }
            }
        }

        else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 801547) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }
}