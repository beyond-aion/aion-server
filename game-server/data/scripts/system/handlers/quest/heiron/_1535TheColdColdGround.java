package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Rolandas, Neon
 */
public class _1535TheColdColdGround extends AbstractQuestHandler {

	public _1535TheColdColdGround() {
		super(1535);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204580).addOnQuestStart(questId);
		qe.registerQuestNpc(204580).addOnTalkEvent(questId);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (targetId != 204580)
			return false;

		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (env.getDialogActionId() == QUEST_SELECT)
				return sendQuestDialog(env, 4762);
			else
				return sendQuestStartDialog(env);
		}

		if (qs.getStatus() == QuestStatus.START) {
			boolean abexSkins = player.getInventory().getItemCountByItemId(182201818) > 4;
			boolean worgSkins = player.getInventory().getItemCountByItemId(182201819) > 2;
			boolean karnifSkins = player.getInventory().getItemCountByItemId(182201820) > 0;

			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					return sendQuestDialog(env, 1352);
				case SETPRO1:
					if (abexSkins && removeQuestItem(env, 182201818, 5)) {
						qs.setRewardGroup(0);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 5);
					}
					return sendQuestDialog(env, 1693);
				case SETPRO2:
					if (worgSkins && removeQuestItem(env, 182201819, 3)) {
						qs.setRewardGroup(1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 6);
					}
					return sendQuestDialog(env, 1693);
				case SETPRO3:
					if (karnifSkins && removeQuestItem(env, 182201820, 1)) {
						qs.setRewardGroup(2);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestDialog(env, 7);
					}
					return sendQuestDialog(env, 1693);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
