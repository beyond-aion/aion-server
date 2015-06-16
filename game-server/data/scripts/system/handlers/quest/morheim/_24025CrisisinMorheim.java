package quest.morheim;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Ritsu
 */
public class _24025CrisisinMorheim extends QuestHandler {

	private final static int questId = 24025;
	private final static int[] npc_ids = { 204388, 204414, 204304, 204345 };

	public _24025CrisisinMorheim() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env, 2039);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 24020, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204388: {
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							else if (var == 2)
								return sendQuestDialog(env, 1693);
						case SETPRO1:
							if (var == 0) {
								return defaultCloseDialog(env, 0, 1); // 1
							}
						case CHECK_USER_HAS_QUEST_ITEM:
							return checkQuestItems(env, 2, 3, false, 10000, 10001);
					}
				}
					break;
				case 204345: {
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 3)
								return sendQuestDialog(env, 2034);
						case SET_SUCCEED:
							if (var == 3) {
								return defaultCloseDialog(env, 3, 3, true, false);
								
							}
					}
				}
					break;
				case 204414: {
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
						case SELECT_ACTION_1354:
							playQuestMovie(env, 85);
							break;
						case SETPRO2:
							if (var == 1) {
								return defaultCloseDialog(env, 1, 2); // 2
							}
					}
				}
					break;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204304) {
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else {
					removeQuestItem(env, 182215370, 1);
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}
}