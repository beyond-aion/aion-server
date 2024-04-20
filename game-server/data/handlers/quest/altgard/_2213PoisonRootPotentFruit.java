package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author Mr. Poke, Majka
 */
public class _2213PoisonRootPotentFruit extends AbstractQuestHandler {

	public _2213PoisonRootPotentFruit() {
		super(2213);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203604).addOnQuestStart(questId);
		qe.registerQuestNpc(203604).addOnTalkEvent(questId);
		qe.registerQuestNpc(700057).addOnTalkEvent(questId);
		qe.registerQuestNpc(203604).addOnTalkEvent(questId);
		qe.addHandlerSideQuestDrop(questId, 700057, 182203208, 1, 100);
		qe.registerOnGetItem(182203208, questId);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 203604) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 700057: // Okaru Tree
					if (env.getDialogActionId() == USE_OBJECT)
						return true; // loot
					return false;
				case 203604:
					if (qs.getQuestVarById(0) == 1) {
						if (env.getDialogActionId() == QUEST_SELECT)
							return sendQuestDialog(env, 2375);
						else if (env.getDialogActionId() == SELECT_QUEST_REWARD) {
							removeQuestItem(env, 182203208, 1);
							player.getEffectController().removeEffect(255); // Remove Okaru Log Poison effect [ID: 255]
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestEndDialog(env);
						} else
							return sendQuestEndDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203604)
				return sendQuestEndDialog(env);
		}
		return false;
	}

	@Override
	public boolean onGetItemEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		SkillEngine.getInstance().applyEffectDirectly(255, player, player); // Add Okaru Log Poison effect [ID: 255]
		return defaultOnGetItemEvent(env, 0, 1, false); // 1
	}
}
