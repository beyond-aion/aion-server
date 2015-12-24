package quest.eltnen;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * @author Artur
 */
public class _14025CookingUpDisasters extends QuestHandler {

	private final static int questId = 14025;
	private final static int[] npcs = { 203989, 203901, 204020, 204024 };
	private final static int[] mobs = { 211017, 211034, 211776, 232133, 217090 };

	public _14025CookingUpDisasters() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnDie(questId);
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		for (int mob : mobs)
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env, 14024);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 14020, 14024 };
		return defaultOnLvlUpEvent(env, quests, true);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		if (targetId == 211017 || targetId == 211034 || targetId == 211776 || targetId == 232133) {
				if (qs.getQuestVarById(0) == 5 && qs.getQuestVarById(1) < 4) {
					qs.setQuestVarById(1, qs.getQuestVarById(1) + 1);
					updateQuestStatus(env);
					return true;
				}
		}
		else if (targetId == 217090)
			if (qs.getQuestVarById(0) == 5 && qs.getQuestVarById(2) < 1) {
				qs.setQuestVarById(2, qs.getQuestVarById(2) + 1);
				updateQuestStatus(env);
				return true;
			}
		return false;
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

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203901) // Telemachus
				return sendQuestEndDialog(env);
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203989) // Tumblusen
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						else if (var == 4)
							return sendQuestDialog(env, 1352);
						else if (var == 5)
							return sendQuestDialog(env, 2716);
					case SELECT_ACTION_1013:
						if (var == 0)
							playQuestMovie(env, 183);
					case SETPRO1:
						if (var == 0)
							return defaultCloseDialog(env, 0, 1); // 1
						else
							return sendQuestDialog(env, 1352);
					case SETPRO2:
						if (var == 1)
							return super.closeDialogWindow(env);
						else
							defaultCloseDialog(env, 4, 5); // 5
					case CHECK_USER_HAS_QUEST_ITEM:
						if (var == 1 && QuestService.collectItemCheck(env, true)) {
							super.changeQuestStep(env, 1, 3, false);
							return sendQuestDialog(env, 1438);
						} else
							return sendQuestDialog(env, 1353);
					case SETPRO6:
						if (var == 5) {
							changeQuestStep(env, 5, 6, true);
							return super.closeDialogWindow(env);
						}
				}
			} else if (targetId == 203901) // Telemachus
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 5)
							return sendQuestDialog(env, 1693);
					case SETPRO3:
						if (var == 5)
							return defaultCloseDialog(env, 5, 6); // 6
				}
			} else if (targetId == 204020) // Mabangtah
			{
				switch (env.getDialog()) {
					case USE_OBJECT:
						if (var == 7)
							return sendQuestDialog(env, 2034);
					case QUEST_SELECT:
						if (var == 3)
							return sendQuestDialog(env, 1693);
						if (var == 6)
							return sendQuestDialog(env, 2034);
						else if (var == 10)
							return sendQuestDialog(env, 3057);
					case SETPRO3:
						if (var == 3) {
							qs.setQuestVarById(0, 5);
							updateQuestStatus(env);
							return true;
						}
					case SETPRO7:
						return defaultCloseDialog(env, 10, 10, true, false); // reward
				}
			} else if (targetId == 204024) // Targatu
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 7)
							return sendQuestDialog(env, 2375);
						else if (var == 9)
							return sendQuestDialog(env, 2716);
					case SETPRO5:
						defaultCloseDialog(env, 7, 8); // 8
					case SETPRO6:
						if (var == 9) {
							TeleportService2.teleportTo(player, 210020000, 1606, 1529, 318);
							qs.setQuestVarById(0, 10); // 10
							updateQuestStatus(env);
							return true;
						}
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (var >= 7 && var <= 10) {
				qs.setQuestVarById(0, 6); // 6
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}
}
