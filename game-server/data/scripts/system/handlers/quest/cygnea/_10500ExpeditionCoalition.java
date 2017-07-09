package quest.cygnea;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @Author Majka
 */
public class _10500ExpeditionCoalition extends AbstractQuestHandler {

	public _10500ExpeditionCoalition() {
		super(10500);
	}

	@Override
	public void register() {
		// Nubes 804968
		// Atmis 804699
		int[] npcs = { 804698, 804699 };
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnLevelChanged(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		// int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		switch (targetId) {
			case 804698: // Nubes
				if (qs.getStatus() == QuestStatus.START) {
					if (dialogActionId == QUEST_SELECT) {
						return sendQuestDialog(env, 1011);
					}

					if (dialogActionId == SET_SUCCEED) {
						qs.setQuestVar(1);
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return closeDialogWindow(env);
					}
				}
				break;
			case 804699: // Atmis
				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}
					return sendQuestEndDialog(env);
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		if (player.getWorldId() == WorldMapType.CYGNEA.getId() && !player.getQuestStateList().hasQuest(questId))
			return QuestService.startQuest(env);
		return false;
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		onEnterWorldEvent(new QuestEnv(null, player, questId));
	}
}
