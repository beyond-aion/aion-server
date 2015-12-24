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
 * Talk to Tumblusen (203989). Get rid of the Kaidan Scouts (212010) to the southeast of the Observatory (3). Talk to Tumblusen. Report to Telemachus
 * (203901). Talk to Mabangtah (204020). Talk to Targatu (204024). Kill the Guard at the Watchtower (204046) and scout the Kaidan Headquarters (1).
 * Return to Targatu. Talk to Mabangtah. Report to Tumblusen.
 * 
 * @author Rhys2002
 * @reworked vlog
 */
public class _1040ScoutingtheScouts extends QuestHandler {

	private final static int questId = 1040;
	private final static int[] npcs = { 203989, 203901, 204020, 204024 };
	private final static int[] mobs = { 212010, 212011, 204046 };

	public _1040ScoutingtheScouts() {
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
		return defaultOnZoneMissionEndEvent(env, 1036);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 1300, 1036 };
		return defaultOnLvlUpEvent(env, quests, true);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (targetId == 212010 || targetId == 212011)
			return defaultOnKillEvent(env, new int[] { 212010, 212011 }, 1, 4); // 2, 3, 4
		else if (targetId == 204046)
			if (defaultOnKillEvent(env, targetId, 8, 9)) // 9
			{
				playQuestMovie(env, 36);
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
			if (targetId == 203989) // Tumblusen
				switch (env.getDialog()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 3398);
					case SELECT_QUEST_REWARD:
						return sendQuestEndDialog(env);
					case SELECTED_QUEST_NOREWARD:
						QuestService.finishQuest(env);
						return closeDialogWindow(env);
				}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203989) { // Tumblusen
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						else if (var == 4)
							return sendQuestDialog(env, 1352);
					case SELECT_ACTION_1013:
						if (var == 0)
							return playQuestMovie(env, 183);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
					case SETPRO2:
						return defaultCloseDialog(env, 4, 5); // 5
				}
			} else if (targetId == 203901) { // Telemachus
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 5)
							return sendQuestDialog(env, 1693);
					case SETPRO3:
						if (defaultCloseDialog(env, 5, 6)) { // 6
							TeleportService2.teleportToNpc(player, 204020);
							return true;
						}
				}
			} else if (targetId == 204020) { // Mabangtah
				switch (env.getDialog()) {
					case USE_OBJECT:
						if (var == 7)
							return sendQuestDialog(env, 2034);
					case QUEST_SELECT:
						if (var == 6)
							return sendQuestDialog(env, 2034);
						else if (var == 10)
							return sendQuestDialog(env, 3057);
					case SETPRO4:
						if (var == 6 || var == 7) {
							TeleportService2.teleportTo(player, 210020000, 2211, 811, 513);
							qs.setQuestVarById(0, 7); // 7
							updateQuestStatus(env);
							return true;
						}
					case SETPRO7:
						return defaultCloseDialog(env, 10, 10, true, false); // reward
				}
			} else if (targetId == 204024) { // Targatu
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 7)
							return sendQuestDialog(env, 2375);
						else if (var == 9)
							return sendQuestDialog(env, 2716);
					case SETPRO5:
						return defaultCloseDialog(env, 7, 8); // 8
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
