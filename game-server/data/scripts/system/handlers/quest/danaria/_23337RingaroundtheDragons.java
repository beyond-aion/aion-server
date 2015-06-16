package quest.danaria;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;


/**
 * @author Mr.madison, Modified by Artur
 *
 */
public class _23337RingaroundtheDragons extends QuestHandler {

	private final static int questId = 23337;
	
		int[] mobs = { 231348, 231349, 231350, 231351, 231352, 231353 };
	
	public _23337RingaroundtheDragons() {
		super(questId);
	}
	
	@Override
	public void register() {
		qe.registerQuestNpc(801053).addOnQuestStart(questId);
		qe.registerQuestNpc(801053).addOnTalkEvent(questId);
		qe.registerQuestNpc(206321).addOnAtDistanceEvent(questId);
        qe.registerOnEnterZone(ZoneName.get("LDF5B_SENSORYAREA_Q13337_206321_2_600060000"), questId);
		for (int mob : mobs)
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
	}
	
	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 801053) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 4762);
					}
					case ASK_QUEST_ACCEPT: {
						return sendQuestDialog(env, 4);
					}
					case QUEST_ACCEPT_1: {
						return sendQuestStartDialog(env);
					}
					case QUEST_REFUSE_1: {
						return sendQuestDialog(env, 1004);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 801053) {
				switch (dialog) {
					case QUEST_SELECT: {
						return sendQuestDialog(env, 1693);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						return checkQuestItems(env, 10, 11, true, 10000, 10001);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 801053) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(0) == 0) {
				changeQuestStep(env, 0, 1, false);
	  		return true;
			}
  	}
		return false;
	}
        
	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, mobs, 1, 10);
	}
    @Override
    public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
        if (zoneName == ZoneName.get("LDF5B_SENSORYAREA_Q13337_206321_2_600060000")) {
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

