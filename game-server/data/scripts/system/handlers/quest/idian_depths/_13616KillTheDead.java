package quest.idian_depths;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Artur
 *
 */
public class _13616KillTheDead extends QuestHandler {

	private final static int questId = 13616;

	public _13616KillTheDead() {
		super(questId);
	}

	public void register() {
		qe.registerQuestNpc(801543).addOnQuestStart(questId);
		qe.registerQuestNpc(801543).addOnTalkEvent(questId);
		qe.registerQuestNpc(206327).addOnAtDistanceEvent(questId);
		qe.registerQuestNpc(230877).addOnKillEvent(questId);
		qe.registerQuestNpc(230987).addOnKillEvent(questId);
		qe.registerQuestNpc(230868).addOnKillEvent(questId);
        qe.registerOnEnterZone(ZoneName.get("LDF5_UNDER_SENSORYAREA_Q13616_206327_2_600070000"),questId);
	}

    @Override
    public boolean onDialogEvent(QuestEnv env) {
        Player player = env.getPlayer();
        int targetId = env.getTargetId();
        QuestState qs = player.getQuestStateList().getQuestState(questId);
        DialogAction dialog = env.getDialog();

        if (qs == null || qs.getStatus() == QuestStatus.NONE) {
            if (targetId == 801543) {
                if (dialog == DialogAction.QUEST_SELECT) {
                    return sendQuestDialog(env, 4762);
                }
                else {
                    return sendQuestStartDialog(env);
                }
            }
        }
        else if (qs.getStatus() == QuestStatus.START) {
            if (targetId == 801543) {

                if (dialog == DialogAction.QUEST_SELECT) {
            if (QuestService.collectItemCheck(env, true)) {
                qs.setStatus(QuestStatus.REWARD);
                updateQuestStatus(env);
                return sendQuestDialog(env, 5);
            }
            else
                return sendQuestDialog(env, 10001);
            }
        }
        }
         else if (qs.getStatus() == QuestStatus.REWARD) {
            if (targetId == 801543) {
                return sendQuestEndDialog(env);
            }
        }
        return false;
    }

    @Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 0) {
				changeQuestStep(env, 0, 1, false);
				return true;
			}
  	}
		return false;
	}

    @Override
    public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
        if (zoneName == ZoneName.get("LDF5_UNDER_SENSORYAREA_Q13616_206327_2_600070000")) {
            Player player = env.getPlayer();
            if (player == null)
                return false;
            QuestState qs = player.getQuestStateList().getQuestState(questId);
            if (qs != null && qs.getStatus() == QuestStatus.START) {
                int var = qs.getQuestVarById(0);
                if (var == 0) {
                    changeQuestStep(env, 0, 1, false);
                    return true;
                }
            }
        }
        return false;
    }
}

