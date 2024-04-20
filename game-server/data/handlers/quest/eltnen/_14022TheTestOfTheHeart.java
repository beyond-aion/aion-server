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
public class _14022TheTestOfTheHeart extends AbstractQuestHandler {

	private final static int[] mob_ids = { 210808, 210799 };

	public _14022TheTestOfTheHeart() {
		super(14022);
	}

	@Override
	public void register() {
		int[] npc_ids = { 203900, 203996 };
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
				case 203900: // Diomedes
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 203996: // Kimeia
					switch (env.getDialogActionId()) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2); // 2
					}
			}
		}
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203996) { // Kimeia
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		if (defaultOnKillEvent(env, mob_ids, 2, 8)) {
			QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
			if (qs.getQuestVarById(0) == 8) {
				qs.setStatus(QuestStatus.REWARD);
				super.updateQuestStatus(env);
			}
			return true;
		} else
			return false;
	}
}
