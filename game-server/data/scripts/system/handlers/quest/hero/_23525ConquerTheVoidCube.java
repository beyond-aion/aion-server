package quest.hero;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

public class _23525ConquerTheVoidCube extends QuestHandler {

    public static final int questId = 23525;

    public _23525ConquerTheVoidCube() {
        super(questId);
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        return defaultOnLvlUpEvent(env);
    }

    @Override
    public void register() {
        qe.registerOnLevelUp(questId);
        qe.registerQuestNpc(800571).addOnQuestStart(questId); // Nieber.
        qe.registerQuestNpc(800571).addOnTalkEvent(questId); // Nieber.
        qe.registerQuestNpc(800571).addOnTalkEvent(questId); // Nieber.
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        DialogAction dialog = env.getDialog();
        int targetId = env.getTargetId();
        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 800571) { // Nieber.
                switch (dialog) {
                    case QUEST_SELECT: {
                        return sendQuestDialog(env, 1011);
                    }
                    case QUEST_ACCEPT_1:
                    case QUEST_ACCEPT_SIMPLE:
                        return sendQuestStartDialog(env);
                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            switch (targetId) {
                case 800571: { // Nieber.
                    switch (dialog) {
                        case QUEST_SELECT: {
                            return sendQuestDialog(env, 2375);
                        }
                        case SELECT_QUEST_REWARD: {
                            changeQuestStep(env, 0, 0, true);
                            return sendQuestEndDialog(env);
                        }
                    }
                }
            }
        } else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 800571) { // Nieber.
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }
}