package quest.enshar;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @Author Majka
 */
public class _25052AnOfferingPeace extends QuestHandler {

	private final static int questId = 25052;

	public _25052AnOfferingPeace() {
		super(questId);
	}

	@Override
	public void register() {
		// Sea Jotun's treasure 731561
		// Redelf 804913
		// Soglo 804915
		qe.registerQuestNpc(804913).addOnQuestStart(questId);
		int[] npcs = { 731561, 804913, 804915 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();
		
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 804913) { // Redelf
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			
			switch(targetId) {
				case 731561: // Sea Jotun's treasure
					if(var == 0) {
						if (dialog == DialogAction.QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialog == DialogAction.SET_SUCCEED) {
							Npc npc = (Npc) env.getVisibleObject();
							if(npc != null)
								QuestService.addNewSpawn(220080000, player.getInstanceId(), 220032, npc.getPosition().getX(), npc.getPosition().getY(), npc.getPosition().getZ(), (byte) 10, 5);
							giveQuestItem(env, 182215721, 1);
							qs.setQuestVar(var+1);
							return defaultCloseDialog(env, var+1, var+1, true, false);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			switch(targetId) {
				case 804915: // Soglo
					if (dialog == DialogAction.USE_OBJECT)
						return sendQuestDialog(env, 10002);
					
					removeQuestItem(env, 182215721, 1);
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}