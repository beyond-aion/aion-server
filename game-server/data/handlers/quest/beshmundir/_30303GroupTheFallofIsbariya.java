package quest.beshmundir;

import static com.aionemu.gameserver.model.DialogAction.QUEST_SELECT;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Gigi
 */
public class _30303GroupTheFallofIsbariya extends AbstractQuestHandler {

	private final static int[] npc_ids = { 799225 };

	public _30303GroupTheFallofIsbariya() {
		super(30303);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(799225).addOnQuestStart(questId);
		qe.registerQuestNpc(216175).addOnKillEvent(questId);
		qe.registerQuestNpc(216177).addOnKillEvent(questId);
		qe.registerQuestNpc(216179).addOnKillEvent(questId);
		qe.registerQuestNpc(216181).addOnKillEvent(questId);
		qe.registerQuestNpc(216182).addOnKillEvent(questId);
		qe.registerQuestNpc(216263).addOnKillEvent(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			if (targetId == 799225) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		}

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 799225) {
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 10002);
				}
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799225)
				return sendQuestEndDialog(env);
			return false;
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		int var = qs.getQuestVarById(0);
		int var1 = qs.getQuestVarById(1);
		int var2 = qs.getQuestVarById(2);
		int var3 = qs.getQuestVarById(3);

		switch (targetId) {
			case 216175:
				if (var == 0 || var1 == 0 || var2 == 1 || var3 == 1 || var1 == 0 || var2 == 0 || var3 == 0) {
					qs.setQuestVarById(0, 1);
					updateQuestStatus(env);
				}
				break;
			case 216177:
				if (var == 1 || var1 == 0 || var2 == 1 || var3 == 1 || var == 0 || var2 == 0 || var3 == 0) {
					qs.setQuestVarById(1, 1);
					updateQuestStatus(env);
				}
				break;
			case 216179:
				if (var == 1 || var1 == 0 || var2 == 0 || var3 == 1 || var == 0 || var1 == 0 || var3 == 0) {
					qs.setQuestVarById(2, 1);
					updateQuestStatus(env);
				}
				break;
			case 216181:
				if (var == 1 || var1 == 0 || var2 == 1 || var3 == 0 || var == 0 || var2 == 0 || var1 == 0) {
					qs.setQuestVarById(3, 1);
					updateQuestStatus(env);
				}
				break;
			case 216182:
			case 216263:
				if (var == 1 && var1 == 1 && var2 == 1 && var3 == 1) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					playQuestMovie(env, 443);
				}
				break;
		}
		return false;
	}
}
