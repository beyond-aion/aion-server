package quest.altgard;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Mr. Poke
 */
public class _2019SecuringtheSupplyRoute extends QuestHandler {

	private final static int questId = 2019;

	public _2019SecuringtheSupplyRoute() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(798033).addOnTalkEvent(questId);
		qe.registerQuestNpc(210492).addOnKillEvent(questId);
		qe.registerQuestNpc(210493).addOnKillEvent(questId);
		qe.registerQuestNpc(203673).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		final int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798033:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							else if (var == 4)
								return sendQuestDialog(env, 1352);
							break;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
						case SETPRO2:
							return defaultCloseDialog(env, 4, 5, 182203024, 1, 0, 0); // 5
					}
				case 203673:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 5)
								return sendQuestDialog(env, 1693);
						case SELECT_QUEST_REWARD:
							if (var == 5) {
								removeQuestItem(env, 182203024, 1);
								changeQuestStep(env, 5, 5, true); // true
								return sendQuestDialog(env, 5);
							}
					}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203673)
				return sendQuestEndDialog(env);
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
			case 210492:
			case 210493:
				if (var >= 1 && var < 4) {
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2200, true);
	}

}
