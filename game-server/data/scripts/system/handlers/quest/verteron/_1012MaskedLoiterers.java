package quest.verteron;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Talk with Spiros (203111). Scout around Verteron Citadel (210030000) for suspicious strangers. Scouting completed! Report back to Spiros. Collect
 * the Revolutionary Symbol (182200010) (5) and take them to Spiros.
 * 
 * @author MrPoke, Dune11
 * @reworked vlog
 */
public class _1012MaskedLoiterers extends QuestHandler {

	private final static int questId = 1012;

	public _1012MaskedLoiterers() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203111).addOnTalkEvent(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1130, true);
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
			if (targetId == 203111) // Spiros
			{
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						if (var == 2)
							return sendQuestDialog(env, 1352);
						if (var == 3)
							return sendQuestDialog(env, 1693);
					case SETPRO1:
						return defaultCloseDialog(env, 0, 1); // 1
					case SETPRO2:
						return defaultCloseDialog(env, 2, 3); // 3
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 3, 3, true, 5, 2034);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203111)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
