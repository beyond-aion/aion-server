package quest.tiamaranta;

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
public class _41526PyroMania extends QuestHandler {

	private final static int questId = 41526;

	public _41526PyroMania() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestSkill(10379, questId);
		qe.registerQuestNpc(205941).addOnQuestStart(questId);
		qe.registerQuestNpc(205941).addOnTalkEvent(questId);
		qe.registerQuestNpc(701235).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205941) { 
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				else if(dialog == DialogAction.QUEST_ACCEPT_SIMPLE) {
					giveQuestItem(env, 182212587, 1);
					return sendQuestStartDialog(env);
				}
				else {
					return sendQuestStartDialog(env);
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 701235) {
				return true; //loot
			}
			else if (targetId == 205941) {
				switch (dialog) {
					case QUEST_SELECT: {
						if(player.getInventory().getItemCountByItemId(182212529) >= 5)
							return sendQuestDialog(env, 1011);
					}
					case CHECK_USER_HAS_QUEST_ITEM: {
						return checkQuestItems(env, 0, 1, true, 5, 10001);
					}
				}
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205941) {
				switch (dialog) {
					case USE_OBJECT: {
						return sendQuestDialog(env, 5);
					}
					default: {
						removeQuestItem(env, 182212587, 1);
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}
		
	@Override
	public boolean onUseSkillEvent(QuestEnv env, int skillUsedId) {
		Player player = env.getPlayer();
		Npc npc = (Npc) player.getTarget();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START && skillUsedId == 10379) {
			if (npc == null && player.getInventory().getItemCountByItemId(182212529) >= 5)
				return false;
			if(player.getTarget() == npc && npc.getName().equals("pyroclast")) {
				npc.getController().die();
				QuestService.addNewSpawn(npc.getWorldId(), npc.getInstanceId(), 701235, npc.getX(), npc.getY(), npc.getZ(), (byte) 0);
				return true;
			}
		}
		return false;
	}
}
