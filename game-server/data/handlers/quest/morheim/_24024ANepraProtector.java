package quest.morheim;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Ritsu
 */
public class _24024ANepraProtector extends AbstractQuestHandler {

	public _24024ANepraProtector() {
		super(24024);
	}

	@Override
	public void register() {
		int[] npc_ids = { 204369, 204361, 278004 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(212861).addOnKillEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("ALTAR_OF_THE_BLACK_DRAGON_220020000"), questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24020);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24020);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204369:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						case SELECT1_1:
							playQuestMovie(env, 80);
							break;
						case SETPRO1:
							if (var == 0) {
								return defaultCloseDialog(env, 0, 1); // 1
							}
					}
					return false;
				case 204361:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO2:
							if (var == 1) {
								return defaultCloseDialog(env, 1, 2); // 2
							}
					}
					return false;
				case 278004:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
					}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204369) {
				if (dialogActionId == USE_OBJECT)
					return sendQuestDialog(env, 10002);
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
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() != QuestStatus.START)
			return false;
		switch (targetId) {
			case 212861:
				if (var == 3) {
					changeQuestStep(env, 3, 3, true); // reward
				}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		if (player == null)
			return false;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (zoneName != ZoneName.get("ALTAR_OF_THE_BLACK_DRAGON_220020000"))
			return false;
		if (qs != null && qs.getQuestVarById(0) == 2) {
			env.setQuestId(questId);
			qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
			updateQuestStatus(env);
			playQuestMovie(env, 81);
			return true;
		}
		return false;
	}
}
