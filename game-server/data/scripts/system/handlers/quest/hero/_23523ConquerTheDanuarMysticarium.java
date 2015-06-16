package quest.hero;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

public class _23523ConquerTheDanuarMysticarium extends QuestHandler {

    public static final int questId = 23523;

    public _23523ConquerTheDanuarMysticarium() {
        super(questId);
    }

    @Override
    public boolean onLvlUpEvent(QuestEnv env) {
        return defaultOnLvlUpEvent(env);
    }

    @Override
    public void register() {
        qe.registerOnEnterWorld(questId);
        qe.registerQuestNpc(800524).addOnQuestStart(questId);
        qe.registerQuestNpc(800524).addOnTalkEvent(questId);
        qe.registerQuestNpc(800524).addOnTalkEvent(questId);
    }

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        QuestState qs1 = player.getQuestStateList().getQuestState(28000);
        QuestState qs2 = player.getQuestStateList().getQuestState(28001);
        QuestState qs3 = player.getQuestStateList().getQuestState(28002);
        QuestState qs4 = player.getQuestStateList().getQuestState(28003);
        DialogAction dialog = env.getDialog();
        int targetId = env.getTargetId();

        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 800524) {
                switch (dialog) {
                    case QUEST_SELECT: {
                        if (qs1 == null || qs2 == null || qs3 == null || qs4 == null) {
                            return closeDialogWindow(env);
                        }

                        if (qs1.getStatus() == QuestStatus.COMPLETE && qs2.getStatus() == QuestStatus.COMPLETE && qs3.getStatus() == QuestStatus.COMPLETE
                                && qs4.getStatus() == QuestStatus.COMPLETE ) {
                            QuestService.startQuest(env);
                            return sendQuestDialog(env, 2375);
                        }
                        else
                            return closeDialogWindow(env);
                    }

                }
            }
        } else if (qs.getStatus() == QuestStatus.START) {
            switch (targetId) {
                case 800524: {
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
            if (targetId == 800524) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }
    @Override
    public boolean onEnterWorldEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        QuestState qs1 = player.getQuestStateList().getQuestState(28000);
        QuestState qs2 = player.getQuestStateList().getQuestState(28001);
        QuestState qs3 = player.getQuestStateList().getQuestState(28002);
        QuestState qs4 = player.getQuestStateList().getQuestState(28003);

        if (qs1 == null || qs2 == null || qs3 == null || qs4 == null) {
            return false;
        }

        if (qs1.getStatus() == QuestStatus.COMPLETE && qs2.getStatus() == QuestStatus.COMPLETE && qs3.getStatus() == QuestStatus.COMPLETE
            && qs4.getStatus() == QuestStatus.COMPLETE ) {
            if (qs == null || qs.getStatus() == QuestStatus.NONE){
                QuestService.startQuest(env);
                return true;
            }
        }
        return false;
    }
}

