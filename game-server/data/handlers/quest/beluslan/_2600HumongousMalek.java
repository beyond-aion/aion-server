package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author VladimirZ
 */
public class _2600HumongousMalek extends AbstractQuestHandler {

	private final static int[] npc_ids = { 204734, 798119, 700512 };

	public _2600HumongousMalek() {
		super(2600);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204734).addOnQuestStart(questId);
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
		if (targetId == 204734) {
			if (qs == null || qs.isStartable()) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		}
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204734) {
				return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 798119) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 0) {
						return sendQuestDialog(env, 1352);
					} else if (var == 1) {
						if (player.getInventory().getItemCountByItemId(182204528) > 0) {
							return sendQuestDialog(env, 1693);
						} else {
							giveQuestItem(env, 182204528, 1);
							return sendQuestDialog(env, 1779);
						}
					}
					return false;
				case SETPRO1:
					return defaultCloseDialog(env, 0, 1, 182204528, 1);
			}
		} else if (targetId == 700512) {
			switch (env.getDialogActionId()) {
				case USE_OBJECT:
					if (var == 1) {
						if (player.getInventory().getItemCountByItemId(182204528) > 0) {
							removeQuestItem(env, 182204528, 1);
							spawnForFiveMinutes(215383, player.getWorldMapInstance(), (float) 1140.78, (float) 432.85, (float) 341.0825, (byte) 0);
							return true;
						}
					}
					return false;
			}
		} else if (targetId == 204734) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if ((var == 1) && (player.getInventory().getItemCountByItemId(182204529) > 0)) {
						return sendQuestDialog(env, 2375);
					} else {
						return sendQuestDialog(env, 2716);
					}
				case SELECT_QUEST_REWARD:
					return removeQuestItem(env, 182204529, 1) && defaultCloseDialog(env, 1, 1, true, true);
			}
		}
		return false;
	}
}
