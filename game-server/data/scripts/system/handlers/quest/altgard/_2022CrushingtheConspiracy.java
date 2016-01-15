package quest.altgard;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;

/**
 * Talk with Suthran (203557). Go to Zemurru's Grave (220030000) and find the Abyss Gate. Then, go into the sealed area.
 * Disrupt the Gate Guardian Stone (700140), which maintains the Abyss Gate(700141). Defeat Kuninasha (214103). Destroy
 * the Abyss Gate(700141). Report the result to Suthran.
 * 
 * @author HGabor85
 * @modified Gigi
 * @reworked vlog
 * @Modified Majka
 */
public class _2022CrushingtheConspiracy extends QuestHandler {

	private final static int questId = 2022;
	private final static int[] npcs = { 203557, 700140, 700141 };

	public _2022CrushingtheConspiracy() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerOnDie(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnMovieEndQuest(154, questId);
		for (int npc : npcs)
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		qe.registerQuestNpc(214103).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203557: // Suthran
					if (dialog == DialogAction.QUEST_SELECT && var == 0) {
						return sendQuestDialog(env, 1011);
					} else if (dialog == DialogAction.SETPRO1) {
						TeleportService2.teleportTo(player, 220030000, 2467.6052f, 2548.0076f, 316.12375f, (byte) 63, TeleportAnimation.FADE_OUT_BEAM);
						changeQuestStep(env, 0, 1, false); // 1
						return closeDialogWindow(env);
					} else if (dialog == DialogAction.SELECT_ACTION_1013) {
						playQuestMovie(env, 66);
						return sendQuestDialog(env, 1013);
					}
					break;
				case 700140: // Gate Guardian Stone
					if (var == 2) {
						if (env.getDialog() == DialogAction.USE_OBJECT) {
							QuestService.addNewSpawn(320030000, player.getInstanceId(), 214103, (float) 260.12, (float) 234.93, (float) 216.00, (byte) 90);
							return useQuestObject(env, 2, 3, false, false); // 3
						}
					}
					break;
				case 700141: // Abyss Gate
					if (var == 4) {
						changeQuestStep(env, var, var, true);
						return playQuestMovie(env, 154);
					}
					break;
			}
		}
		else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203557) { // Suthran
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 1352);
				}
				else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var >= 2 && var != 5 && player.getWorldId() != 320030000) {
				changeQuestStep(env, var, 1, false);
				return true;
			}
			else if (var == 1 && player.getWorldId() == 320030000) {
				changeQuestStep(env, 1, 2, false); // 2
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 214103, 3, 4); // 4
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var >= 2) {
				qs.setQuestVar(1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		
		if (movieId == 154 && qs != null) {
			TeleportService2.teleportTo(env.getPlayer(), 220030000, 1683.2405f, 1757.608f, 259.44543f, (byte) 64, TeleportAnimation.FADE_OUT_BEAM);
			return true;
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		int[] quests = { 2021, 2020, 2019, 2018, 2017, 2016, 2015, 2014, 2013, 2012, 2011 };
		return defaultOnLvlUpEvent(env, quests, true); // Sets as zone mission to avoid it appears on new player list.
	}
}
