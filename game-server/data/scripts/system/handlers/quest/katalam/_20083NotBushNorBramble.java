package quest.katalam;

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
 */
public class _20083NotBushNorBramble extends QuestHandler {

	private final static int questId = 20083;
	

	public _20083NotBushNorBramble() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcIds = { 800537, 800539, 800544, 730709, 730710 };
		qe.registerOnLevelUp(questId);
		for (int npcId : npcIds) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}
	
	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 20082);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (targetId == 800537) {
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 0) {
							return sendQuestDialog(env, 1011);
						}
					}
					case SETPRO1: {
						return defaultCloseDialog(env, 0, 1);
					}
				}
			}
			else if (targetId == 800539) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 1) {
							return sendQuestDialog(env, 1352);
						}
					}
					case SETPRO2: {
						return defaultCloseDialog(env, 1, 2); 
					}
				}
			}
			else if (targetId == 800544) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 2) {
							return sendQuestDialog(env, 1693);
						}
					}
					case SETPRO3: {
						return defaultCloseDialog(env, 2, 3); 
					}
				}
			}
			else if (targetId == 730709) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 3) {
							return sendQuestDialog(env, 2375);
						}
					}
					case SETPRO5: {
						Npc npc = (Npc) env.getVisibleObject();
						QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 230392, npc.getX() + 2, npc.getY() - 2, npc.getZ(), (byte) 0);
						QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 230391, npc.getX() - 2, npc.getY() + 2, npc.getZ(), (byte) 0);
						return defaultCloseDialog(env, 3, 4); 
					}
				}
			}
			else if (targetId == 730710) { 
				switch (dialog) {
					case QUEST_SELECT: {
						if (qs.getQuestVarById(0) == 4) {
							return sendQuestDialog(env, 2034);
						}
					}
					case SETPRO4: {
						giveQuestItem(env, 182215236, 1);
						return defaultCloseDialog(env, 4, 4, true, false); 
					}
				}
			}
		}
		else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 800544) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				else {
					removeQuestItem(env, 182215236, 1);
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}
