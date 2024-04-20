package quest.ishalgen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Mr. Poke, Majka
 */
public class _2007WheresRaeThisTime extends AbstractQuestHandler {

	public _2007WheresRaeThisTime() {
		super(2007);
	}

	@Override
	public void register() {
		int[] talkNpcs = { 203516, 203519, 203539, 203552, 203554, 700085, 700086, 700087 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		for (int id : talkNpcs)
			qe.registerQuestNpc(id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203516:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
							return false;
						case SETPRO1:
							if (var == 0) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								closeDialogWindow(env);
								return true;
							}
					}
					break;
				case 203519:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							return false;
						case SETPRO2:
							if (var == 1) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								closeDialogWindow(env);
								return true;
							}
					}
					break;
				case 203539:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
							return false;
						case SELECT3_1:
							playQuestMovie(env, 55);
							break;
						case SETPRO3:
							if (var == 2) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								closeDialogWindow(env);
								return true;
							}
					}
					break;
				case 203552:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 3)
								return sendQuestDialog(env, 2034);
							return false;
						case SETPRO4:
							if (var == 3) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								closeDialogWindow(env);
								return true;
							}
					}
					break;
				case 203554:
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 4)
								return sendQuestDialog(env, 2375);
							else if (var == 8)
								return sendQuestDialog(env, 2716);
							return false;
						case SETPRO5:
							if (var == 4) {
								qs.setQuestVar(5);
								updateQuestStatus(env);
								closeDialogWindow(env);
								return true;
							}
							break;
						case SETPRO6:
							if (var == 8) {
								qs.setQuestVar(9);
								updateQuestStatus(env);
								qs.setQuestVar(8);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								closeDialogWindow(env);
								TeleportService.teleportToNpc(player, 203516);
								return true;
							}
					}
					break;
				case 700085:
					if (var == 5) {
						destroy(6, env);
						return false;
					}
					break;
				case 700086:
					if (var == 6) {
						destroy(7, env);
						return false;
					}
					break;
				case 700087:
					if (var == 7) {
						destroy(-1, env);
						return false;
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203516) {
				if (dialogActionId == USE_OBJECT) {
					playQuestMovie(env, 58);
					return sendQuestDialog(env, 3057);
				} else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		int[] quests = { 2006, 2005, 2004, 2003, 2002, 2001 };
		defaultOnQuestCompletedEvent(env, quests);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		int[] quests = { 2100, 2006, 2005, 2004, 2003, 2002, 2001 };
		defaultOnLevelChangedEvent(player, quests);
	}

	private void destroy(final int var, final QuestEnv env) {
		Player player = env.getPlayer();
		// sendEmotion(env, player, EmotionId.STAND, true); //wrong emotion and source of it - rechk on retail
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		switch (var) {
			case 6:
			case 7:
				qs.setQuestVar(var);
				break;
			case -1:
				playQuestMovie(env, 56);
				qs.setQuestVar(8);
				break;
		}
		updateQuestStatus(env);
	}

}
