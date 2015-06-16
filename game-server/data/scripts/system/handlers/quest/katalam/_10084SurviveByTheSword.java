package quest.katalam;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;


public class _10084SurviveByTheSword extends QuestHandler {

	private final static int questId = 10084;
	
	private final static int[] mobs = {230407, 230408};
	

	public _10084SurviveByTheSword() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 800548, 800549, 800552, 800550, 800561, 701538, 800541 };
		qe.registerQuestItem(182215224, questId);
		qe.registerQuestNpc(206284).addOnAtDistanceEvent(questId);
		qe.registerOnLevelUp(questId);
        qe.registerOnEnterZone(ZoneName.get("LDF5A_SENSORYAREA_Q10084_206284_4_600050000"),questId);
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 10083);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 800548) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 0) {
							return sendQuestDialog(env, 1011);
						}
						else if (qs.getQuestVarById(0) == 1) {
							return sendQuestDialog(env, 1352);
						}
						else if (qs.getQuestVarById(0) == 6) {
							return sendQuestDialog(env, 2034);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
					case SETPRO4: {
						return defaultCloseDialog(env, 6, 7);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						return checkQuestItems(env, 1, 2, false, 10000, 10001);
					}
				}
			}
			else if (targetId == 800549) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 7) {
							return sendQuestDialog(env, 2375);
						}
					}
					case SETPRO5: {
						giveQuestItem(env, 182215224, 1);
						return defaultCloseDialog(env, 7, 8); 
					}
				}
			}
			else if (targetId == 800552) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 9) {
							return sendQuestDialog(env, 3057);
						}
					}
					case SETPRO7: {
						return defaultCloseDialog(env, 9, 10); 
					}
				}
			}
			else if (targetId == 800550) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 10) {
							return sendQuestDialog(env, 3398);
						}
					}
					case SETPRO8: {
						giveQuestItem(env, 182215228, 1);
						return defaultCloseDialog(env, 10, 11); 
					}
				}
			}
			else if (targetId == 800561) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 12) {
							return sendQuestDialog(env, 4080);
						}
					}
					case SETPRO10: {
						return defaultCloseDialog(env, 12, 13); 
					}
				}
			}
			else if (targetId == 701538) { 
				switch (dialog) {
					case USE_OBJECT: {
						if (qs.getQuestVarById(0) == 13) {
							return sendQuestDialog(env, 4082);
						}
					}
					case SETPRO11: {
						giveQuestItem(env, 182215229, 1);
						return defaultCloseDialog(env, 13, 14, true, false); 
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800541) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 5);
				}
				else {
					removeQuestItem(env, 182215229, 1);
					removeQuestItem(env, 182215228, 1);
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, mobs, 2, 6, 0);
	}
	
	@Override
	public HandlerResult onItemUseEvent(QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getQuestVarById(0) == 8) {
			changeQuestStep(env, 8, 9, false);
			removeQuestItem(env, 182215224, 1);
		  return HandlerResult.SUCCESS;
		}
		return HandlerResult.FAILED;
	}
	
	@Override
	public boolean onAtDistanceEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (qs.getQuestVarById(0) == 11) {
				changeQuestStep(env, 11, 12, false);
	  		return true;
			}
  	}
		return false;
	}
    @Override
    public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
        if (zoneName == ZoneName.get("LDF5A_SENSORYAREA_Q10084_206284_4_600050000")) {
            Player player = env.getPlayer();
            if (player == null)
                return false;
            QuestState qs = player.getQuestStateList().getQuestState(questId);
            if (qs != null && qs.getStatus() == QuestStatus.START) {
                int var = qs.getQuestVarById(0);
                if (var == 11) {
                    changeQuestStep(env, 11, 12, false);
                    return true;
                }
            }
        }
        return false;
    }
}
