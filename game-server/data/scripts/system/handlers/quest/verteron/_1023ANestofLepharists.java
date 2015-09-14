package quest.verteron;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Mr. Poke
 * @modified Dune11
 * @reworked vlog
 */
public class _1023ANestofLepharists extends QuestHandler {

	private final static int questId = 1023;

	public _1023ANestofLepharists() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203098).addOnTalkEvent(questId);
		qe.registerQuestNpc(203183).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("MYSTERIOUS_SHIPWRECK_210030000"), questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env, 1013);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 1130, 1013 };
		return defaultOnLvlUpEvent(env, quests, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203098) // Spatalos
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
					case SELECT_ACTION_1012:
						return sendQuestDialog(env, 1012);
					case SELECT_ACTION_1013:
						return sendQuestDialog(env, 1013);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
				}
			} else if (targetId == 203183) // Khidia
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1352);
						else if (var == 3)
							return sendQuestDialog(env, 1693);
						else if (var == 4)
							return sendQuestDialog(env, 2034);
						else if (var == 5)
							return sendQuestDialog(env, 2120);
					case SELECT_ACTION_1353:
						return sendQuestDialog(env, 1353);
					case SETPRO2:
						if (var == 1) {
							playQuestMovie(env, 30);
							return defaultCloseDialog(env, 1, 2); // 2
						}
					case SELECT_ACTION_1694:
						return sendQuestDialog(env, 1694);
					case SETPRO3:
						if (var == 3)
							return defaultCloseDialog(env, 3, 4); // 4
					case CHECK_USER_HAS_QUEST_ITEM:
						if (var == 4)
							return checkQuestItems(env, 4, 5, false, 2120, 2035);
					case FINISH_DIALOG:
						return sendQuestDialog(env, 10);
					case SETPRO4:
						return defaultCloseDialog(env, 5, 5, true, false);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203098) // Spatalos
			{
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 2375);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName != ZoneName.get("MYSTERIOUS_SHIPWRECK_210030000"))
			return false;
		final Player player = env.getPlayer();
		if (player == null)
			return false;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getQuestVars().getQuestVars() != 2)
			return false;

		if (qs.getQuestVars().getVarById(0) == 2) {
			playQuestMovie(env, 23);
			qs.setQuestVarById(0, 3); // 3
			updateQuestStatus(env);
			return true;
		}
		return false;
	}
}
