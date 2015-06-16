package quest.sarpan;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;


/**
 * @author Cheatkiller
 *
 */
public class _41304BringOnTheHungryPagatis extends QuestHandler {

	private final static int questId = 41304;

	public _41304BringOnTheHungryPagatis() {
		super(questId);
	}

	public void register() {
		qe.registerQuestSkill(10390, questId);
		qe.registerQuestNpc(205794).addOnQuestStart(questId);
		qe.registerQuestNpc(205794).addOnTalkEvent(questId);
		qe.registerQuestNpc(218157).addOnKillEvent(questId);
		qe.registerQuestNpc(218156).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205794) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else {
					return sendQuestStartDialog(env, 182213111, 1);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205794) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 10002);
					}
					default: {
						removeQuestItem(env, 182213111, 1);
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			Npc npc = (Npc) env.getVisibleObject();
			int targetId = env.getTargetId();
			if(targetId == 218157 || targetId == 218156) {
				QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 701168, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
			  return true;
			}
		}
		return false;
	}
		
	@Override
	public boolean onUseSkillEvent(QuestEnv env, int skillUsedId) {
		Player player = env.getPlayer();
		Npc npc = (Npc) player.getTarget();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if(player.getTarget() == npc && npc.getObjectTemplate().getTemplateId() == 701168) {
				if(var < 9)
					changeQuestStep(env, var, var + 1, false);
				else
					changeQuestStep(env, 9, 10, true);
				return true;
			}
		}
		return false;
	}
}
