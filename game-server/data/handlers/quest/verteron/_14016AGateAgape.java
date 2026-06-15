package quest.verteron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService;

/**
 * @author Artur, Majka
 */
public class _14016AGateAgape extends AbstractQuestHandler {

	public _14016AGateAgape() {
		super(14016);
	}

	@Override
	public void register() {
		int[] npcs = { 203098, 700142 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		qe.registerQuestNpc(233873).addOnKillEvent(questId);
		for (int npcId : npcs) {
			qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203098: // Spatalos
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO1:
							TeleportService.teleportTo(player, 210030000, 2683.2085f, 1068.8977f, 199.375f);
							changeQuestStep(env, 0, 1); // 1
							return closeDialogWindow(env);
					}
					break;
				case 700142: // Abyss Gate Guardian Stone
					if (dialogActionId == USE_OBJECT) {
						if (QuestService.collectItemCheck(env, true)) {
							return playQuestMovie(env, 153);
						}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203098) { // Spatalos
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 2034);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 2) {
				changeQuestStep(env, 2, 1);
				removeQuestItem(env, 182215317, 1);
				return true;
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
			if (var == 2 && player.getWorldId() != 310030000) {
				changeQuestStep(env, 2, 1);
				removeQuestItem(env, 182215317, 1);
				return true;
			} else if (var == 1 && player.getWorldId() == 310030000) {
				changeQuestStep(env, 1, 2); // 2
				spawnForFiveMinutes(233873, player.getWorldMapInstance(), (float) 258.89917, (float) 237.20166, (float) 217.06035, (byte) 0);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				if (env.getTargetId() == 233873) {
					if (player.getInventory().getItemCountByItemId(182215317) < 1) {
						return giveQuestItem(env, 182215317, 1);
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId != 153)
			return;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			qs.setRewardGroup(0);
			changeQuestStep(env, 2, 2, true); // reward
			TeleportService.teleportTo(player, 210030000, 2683.2085f, 1068.8977f, 199.375f);
		}
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		int[] verteronQuests = { 14015, 14014, 14013, 14012, 14011, 14010 };
		defaultOnQuestCompletedEvent(env, verteronQuests);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		int[] verteronQuests = { 14015, 14014, 14013, 14012, 14011, 14010 };
		defaultOnLevelChangedEvent(player, verteronQuests);
	}
}
