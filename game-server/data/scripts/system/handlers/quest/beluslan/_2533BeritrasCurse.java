package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 */
public class _2533BeritrasCurse extends AbstractQuestHandler {

	public _2533BeritrasCurse() {
		super(2533);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204801).addOnQuestStart(questId); // Gigrite
		qe.registerQuestNpc(204801).addOnTalkEvent(questId);
		qe.registerQuestItem(182204425, questId);// Empty Durable Potion Bottle
		qe.registerOnQuestTimerEnd(questId);
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {

		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideZone(ZoneName.get("BERITRAS_WEAPON_220040000"))) {
				QuestService.questTimerStart(env, 300);
				return HandlerResult.fromBoolean(useQuestItem(env, item, 0, 1, false, 182204426, 1, 0));
			}
		}
		return HandlerResult.SUCCESS; // ??
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 204801) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else if (dialogActionId == QUEST_ACCEPT_1) {
					if (!giveQuestItem(env, 182204425, 1))
						return true;
					return sendQuestStartDialog(env);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204801) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 1352);
						}
						return false;
					case SELECT_QUEST_REWARD:
						QuestService.questTimerEnd(env);
						return sendQuestDialog(env, 5);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204801) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			removeQuestItem(env, 182204426, 1);
			QuestService.abandonQuest(player, questId);
			return true;
		}
		return false;
	}
}
