package quest.hero;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

public class _23527OldSpiritsOldGrudges extends QuestHandler {

	private final static int questId = 23527;

	public _23527OldSpiritsOldGrudges() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(801949).addOnQuestStart(questId);
		qe.registerQuestNpc(801544).addOnTalkEvent(questId);
		qe.registerQuestNpc(233302).addOnKillEvent(questId);
		qe.registerQuestNpc(233303).addOnKillEvent(questId);
		qe.registerQuestNpc(233304).addOnKillEvent(questId);
		qe.registerQuestNpc(233305).addOnKillEvent(questId);
    qe.registerOnQuestTimerEnd(questId);
	}

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        DialogAction dialog = env.getDialog();

        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 801949) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 4762);
                }
                if (dialog == DialogAction.QUEST_ACCEPT_SIMPLE || dialog == DialogAction.QUEST_ACCEPT) {
                    QuestService.questTimerStart(env, 1800);   //TODO Check timer
                    QuestService.startQuest(env);
                    updateQuestStatus(env);
                    return closeDialogWindow(env);
                }
                else {
                    return sendQuestStartDialog(env);
                }
            }
        }
        else if (qs != null && qs.getStatus() == QuestStatus.START ) {
            if (targetId == 801949) {
                if (dialog == DialogAction.QUEST_SELECT)
                    return sendQuestDialog(env, 1352);
            }
            if (dialog == DialogAction.SET_SUCCEED)    {
            changeQuestStep(env, 0, 1, true);
            updateQuestStatus(env);
            PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
            return true;
            }
        }
        else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 801544) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

    @Override
    public boolean onKillEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs == null || qs.getStatus() != QuestStatus.START)
            return false;

        int targetId = 0;
        int var = 0;
        if (env.getVisibleObject() instanceof Npc)
            targetId = ((Npc) env.getVisibleObject()).getNpcId();
        switch (targetId) {
            case 233302:
                var = qs.getQuestVarById(1);
                if (var == 0) {
                    qs.setQuestVarById(1, 1);
                    updateQuestStatus(env);
                }
                if (qs.getQuestVarById(1) == 1 && qs.getQuestVarById(2)== 1 && qs.getQuestVarById(3)== 1 && qs.getQuestVarById(4)== 1)
                {
                    QuestService.questTimerEnd(env);
                }
                break;
            case 233303:
                var = qs.getQuestVarById(2);
                if (var < 1) {
                    qs.setQuestVarById(2, var + 1);
                    updateQuestStatus(env);
                }
                if (qs.getQuestVarById(1) == 1 && qs.getQuestVarById(2)== 1 && qs.getQuestVarById(3)== 1 && qs.getQuestVarById(4)== 1)
                {
                    QuestService.questTimerEnd(env);
                }
                break;
            case 233304:
                var = qs.getQuestVarById(3);
                if (var < 1) {
                    qs.setQuestVarById(3, var + 1);
                    updateQuestStatus(env);
                }
                if (qs.getQuestVarById(1) == 1 && qs.getQuestVarById(2)== 1 && qs.getQuestVarById(3)== 1 && qs.getQuestVarById(4)== 1)
                {
                    QuestService.questTimerEnd(env);
                }
            case 233305:
                var = qs.getQuestVarById(4);
                if (var < 1) {
                    qs.setQuestVarById(4, var + 1);
                    updateQuestStatus(env);
                }
                if (qs.getQuestVarById(1) == 1 && qs.getQuestVarById(2)== 1 && qs.getQuestVarById(3)== 1 && qs.getQuestVarById(4)== 1)
                {
                    QuestService.questTimerEnd(env);
                }
        }
        return false;
    }

    @Override
    public boolean onQuestTimerEndEvent(QuestEnv env) {
        Player player = env.getPlayer();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        if (qs != null && qs.getStatus() == QuestStatus.START) {

            if (qs.getQuestVarById(1) != 1 && qs.getQuestVarById(2)!= 1 && qs.getQuestVarById(3)!= 1 && qs.getQuestVarById(4)!= 1)
            {
                QuestService.abandonQuest(player, questId);
                player.getController().updateNearbyQuests();
                return true;
            }
        }
        return false;
    }
}