package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 */
public class _1573SomeTastyMushrooms extends AbstractQuestHandler {

	public _1573SomeTastyMushrooms() {
		super(1573);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182201784, questId);
		qe.registerQuestNpc(730025).addOnQuestStart(questId);
		qe.registerQuestNpc(730025).addOnTalkEvent(questId);
		qe.registerQuestNpc(700194).addOnTalkEvent(questId);
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideItemUseZone(ZoneName.get("LF3_ITEMUSEAREA_Q1573"))) {
				removeQuestItem(env, 182201784, 1);
				return HandlerResult.fromBoolean(useQuestItem(env, item, 1, 2, false, 182201735, 1, 0));
			}
		}
		return HandlerResult.SUCCESS;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 730025) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 700194) {
				return true;
			}
			if (targetId == 730025) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
						if (var == 2) {
							removeQuestItem(env, 182201735, 1);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 10002);
						}
						return false;
					case CHECK_USER_HAS_QUEST_ITEM:
						return checkQuestItems(env, 0, 1, false, 0, 10001, 182201784, 1);
					case SELECT_QUEST_REWARD:
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 730025)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
