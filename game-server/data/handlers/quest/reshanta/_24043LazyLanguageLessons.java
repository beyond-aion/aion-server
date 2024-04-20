package quest.reshanta;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnSearchResult;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Artur, Majka
 */
public class _24043LazyLanguageLessons extends AbstractQuestHandler {

	public _24043LazyLanguageLessons() {
		super(24043);
	}

	@Override
	public void register() {
		int[] npcs = { 278003, 278086, 278039, 279027, 204210 };
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerQuestNpc(253610).addOnAttackEvent(questId);
		qe.registerQuestNpc(253611).addOnAttackEvent(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 278003: // Hisui
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
					}
					break;
				case 278086: // Sinjah
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2); // 2
					}
					break;
				case 278039: // Grunn
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							return false;
						case SETPRO4:
							return defaultCloseDialog(env, 3, 4); // 4
					}
					break;
				case 279027: // Kaoranerk
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							} else if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
							return false;
						case SELECT7_1:
							removeQuestItem(env, 182215373, 1);
							playQuestMovie(env, 293);
							return sendQuestDialog(env, 3058);
						case SETPRO5:
							return defaultCloseDialog(env, 4, 5); // 5
						case SET_SUCCEED:
							return defaultCloseDialog(env, 6, 6, true, false); // reward
					}
					break;
				case 204210: // Phosphor
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 5) {
								return sendQuestDialog(env, 2716);
							}
							return false;
						case SETPRO6:
							return defaultCloseDialog(env, 5, 6, 182215373, 1, 0, 0); // 6
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 278003) { // Hisui
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onAttackEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				Npc npc = (Npc) env.getVisibleObject();
				SpawnSearchResult searchResult = DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(npc.getWorldId(), 278086); // Sinjah
				if (PositionUtil.isInRange(npc, searchResult.getSpot().getX(), searchResult.getSpot().getY(), searchResult.getSpot().getZ(), 15)) {
					npc.getController().die(player);
					changeQuestStep(env, 2, 3); // 3
					return true;
				}
			}
		}
		return true;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 24040);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 24040);
	}
}
