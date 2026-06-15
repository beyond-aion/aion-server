package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Artur, Majka
 */
public class _14046PiecingTheMemory extends AbstractQuestHandler {

	private final static int[] npc_ids = { 278500, 203834, 203786, 203754, 203704 };

	public _14046PiecingTheMemory() {
		super(14046);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnEnterWorld(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 14040);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 14040);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203704) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				return sendQuestEndDialog(env);
			}
			return false;
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 278500) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
					return false;
				case SETPRO1:
					if (var == 0) {
						TeleportService.teleportTo(player, 110010000, 2014f, 1493f, 581.1387f, (byte) 70, TeleportAnimation.FADE_OUT_BEAM);
						changeQuestStep(env, 0, 1);
						return true;
					}
			}
		} else if (targetId == 203834) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					else if (var == 3)
						return sendQuestDialog(env, 2034);
					else if (var == 5)
						return sendQuestDialog(env, 2716);
					return false;
				case SELECT2_1:
					playQuestMovie(env, 102);
					break;
				case SETPRO2:
					if (var == 1) {
						changeQuestStep(env, 1, 2);
						return closeDialogWindow(env);
					}
					return false;
				case SETPRO4:
					if (var == 3) {
						TeleportService.teleportTo(player, 310070000, 214f, 279f, 1387.241f, (byte) 69, TeleportAnimation.FADE_OUT_BEAM);
						changeQuestStep(env, 3, 4);
						return true;
					}
					return false;
				case SETPRO6:
					if (var == 5) {
						changeQuestStep(env, 5, 6);
						return closeDialogWindow(env);
					}
			}
		} else if (targetId == 203786) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 2)
						return sendQuestDialog(env, 1693);
					return false;
				case CHECK_USER_HAS_QUEST_ITEM:
					return checkQuestItems(env, 2, 3, false, 10000, 10001, 182215354, 1);
			}
		} else if (targetId == 203754) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 6)
						return sendQuestDialog(env, 3057);
					return false;
				case SET_SUCCEED:
					if (var == 6) {
						removeQuestItem(env, 182215354, 1);
						changeQuestStep(env, 6, 6, true);
						return closeDialogWindow(env);
					}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		final QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVarById(0) != 4)
			return false;
		return playQuestMovie(env, 170);
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 170) {
			changeQuestStep(env, 4, 5);
			TeleportService.teleportTo(env.getPlayer(), 110010000, 2014f, 1493f, 581.1387f, (byte) 92, TeleportAnimation.FADE_OUT_BEAM);
		}
	}

}
