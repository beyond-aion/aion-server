package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Artur, Majka
 */
public class _14021ToCureACurse extends AbstractQuestHandler {

	private final static int[] mob_ids = { 210771, 210758, 210763, 210764, 210759, 210770 };

	public _14021ToCureACurse() {
		super(14021);
	}

	@Override
	public void register() {
		int[] npc_ids = { 203902, 700179, 204043, 204030 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnLevelChanged(questId);
		for (int npc : npc_ids)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		for (int mob_id : mob_ids)
			qe.registerQuestNpc(mob_id).addOnKillEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14020);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14020);
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
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 700179: // Paper Glider
					if (var == 7) {
						switch (env.getDialogActionId()) {
							case USE_OBJECT:
								return sendQuestDialog(env, 2034);
							case SELECT4_1:
								changeQuestStep(env, 7, 8); // 7
								return sendQuestDialog(env, 0);
						}
					}
					break;
				case 204043: // Melginie
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 8)
								return sendQuestDialog(env, 2036);
							return false;
						case SETPRO4:
							return defaultCloseDialog(env, 8, 9); // 8
					}
					break;
				case 204030: // Celestine
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 9)
								return sendQuestDialog(env, 2376);
							return false;
						case SETPRO5:
							return defaultCloseDialog(env, 9, 9, true, false); // reward
					}

			}
		}
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203902) { // Aurelius
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2716);
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
			if (var == 10) {
				changeQuestStep(env, 10, 9);
			}
		}
		return false;
	}
}
