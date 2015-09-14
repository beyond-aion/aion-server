package quest.eltnen;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Talk with Aurelius (203902). Hunt Manduri (6): 210771, 210758, 210763, 210764, 210759, 210770 Report to Aurelius. Talk with Archelaos (203936).
 * Find Paper Glider (700179). Find Melginie (204043). Escort Melginie to Celestine (204030). Talk with Celestine. Report to Aurelius.
 * 
 * @author Xitanium
 * @reworked vlog
 */
public class _1031TheMandurisSecret extends QuestHandler {

	private final static int questId = 1031;
	private final static int[] mob_ids = { 210771, 210758, 210763, 210764, 210759, 210770 };
	private final static int[] npc_ids = { 203902, 203936, 700179, 204043, 204030 };

	public _1031TheMandurisSecret() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnLevelUp(questId);
		for (int npc : npc_ids)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		for (int mob_id : mob_ids)
			qe.registerQuestNpc(mob_id).addOnKillEvent(questId);
		qe.registerAddOnReachTargetEvent(questId);
		qe.registerAddOnLostTargetEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1300, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203902: // Aurelius
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							if (var == 7)
								return sendQuestDialog(env, 1352);
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
						case SETPRO2:
							return defaultCloseDialog(env, 7, 8); // 8
					}
					break;
				case 203936: // Archelaos
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 8)
								return sendQuestDialog(env, 1693);
						case SETPRO3:
							return defaultCloseDialog(env, 8, 9); // 9
					}
					break;
				case 700179: // Paper Glider
					if (var == 9) {
						switch (env.getDialog()) {
							case USE_OBJECT:
								return sendQuestDialog(env, 2034);
							case SETPRO4: {
								changeQuestStep(env, 9, 10, false); // 10
								return sendQuestDialog(env, 0);
							}
						}
					}
					break;
				case 204043: // Melginie
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 10)
								return sendQuestDialog(env, 2375);
						case SETPRO5:
							return defaultStartFollowEvent(env, (Npc) env.getVisibleObject(), 204030, 10, 11); // 11
					}
					break;
				case 204030: // Celestine
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 12)
								return sendQuestDialog(env, 3057);
						case SETPRO7:
							return defaultCloseDialog(env, 12, 12, true, false); // reward
					}

			}
		}
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203902) // Aurelius
			{
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 3398);
				else
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

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		switch (targetId) {
			case 210771:
			case 210758:
			case 210763:
			case 210764:
			case 210759:
			case 210770:
				if (var >= 1 && var <= 6) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 11) {
				changeQuestStep(env, 11, 10, false);
			}
		}
		return false;
	}

	@Override
	public boolean onNpcReachTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 11, 12, false); // 12
	}

	@Override
	public boolean onNpcLostTargetEvent(QuestEnv env) {
		return defaultFollowEndEvent(env, 11, 10, false); // 10
	}
}
