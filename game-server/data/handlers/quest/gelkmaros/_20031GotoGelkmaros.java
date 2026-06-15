package quest.gelkmaros;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @Author Majka
 */
public class _20031GotoGelkmaros extends AbstractQuestHandler {

	private final static int[] mobs = { 216091, 216092, 216093, 216095, 216096, 216097 };

	public _20031GotoGelkmaros() {
		super(20031);
	}

	@Override
	public void register() {
		// Vidar ID: 204052
		// Agehia ID: 798800
		// Tigrina ID: 798409
		// Richelle ID: 799225
		// Merhen ID: 799364
		// Hogidin ID: 799365
		// Valetta ID: 799226
		int[] npcs = { 204052, 798409, 798800, 799225, 799226, 799364, 799365 };
		qe.registerOnLevelChanged(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(182215590, questId);
		qe.registerOnEnterWorld(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204052) { // Vidar
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) {
							return sendQuestDialog(env, 1011);
						}
						break;
					case SETPRO1:
						return defaultCloseDialog(env, var, var + 1); // 1
				}
			} else if (targetId == 798800) { // Agehia
				switch (env.getDialogActionId()) {
					case QUEST_SELECT:
						if (var == 1) {
							return sendQuestDialog(env, 1352);
						}
						break;
					case SETPRO2:
						return defaultCloseDialog(env, var, var + 1); // 2
				}
			} else if (targetId == 798409) { // Tigrina
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 2) {
							return sendQuestDialog(env, 1693);
						}
						break;
					case SETPRO3:
						qs.setQuestVar(var + 1); // 3
						updateQuestStatus(env);
						TeleportService.teleportTo(player, 220070000, player.getInstanceId(), 1868, 2746, 531, (byte) 20);
						return closeDialogWindow(env); // 1
				}
			} else if (targetId == 799225) { // Richelle
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 4) {
							return sendQuestDialog(env, 2375);
						}
						break;
					case SETPRO5:
						return defaultCloseDialog(env, var, var + 1); // 5
				}
			} else if (targetId == 799364) { // Merhen
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 5) {
							return sendQuestDialog(env, 2716);
						}
						break;
					case SELECT6_1:
						playQuestMovie(env, 31, true);
						return sendQuestDialog(env, 2717);
					case SETPRO6:
						return defaultCloseDialog(env, var, var + 1); // 6
				}
			} else if (targetId == 799365) { // Hogidin
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 6) {
							return sendQuestDialog(env, 3057);
						}
						break;
					case SETPRO7:
						return defaultCloseDialog(env, var, var + 1); // 7
				}
			} else if (targetId == 799226) { // Valetta
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 7) {
							return sendQuestDialog(env, 3398);
						} else if (var == 10) {
							return sendQuestDialog(env, 6500);
						}
						break;
					case SETPRO8:
						if (var == 7) {
							giveQuestItem(env, 182215590, 1);
							return defaultCloseDialog(env, var, var + 1); // 8
						}
						break;
					case CHECK_USER_HAS_QUEST_ITEM:
						if (var == 10) {
							if (checkItemExistence(env, 182215591, 1, true)) {
								qs.setQuestVar(var + 1); // 11
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
						break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799225) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
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
			if (var == 8) {
				int var1 = qs.getQuestVarById(1);
				if (var1 >= 0 && var1 < 9) {
					return defaultOnKillEvent(env, mobs, var1, var1 + 1, 1);
				} else if (var1 == 9) {
					qs.setQuestVar(9); // 9
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (player.getWorldId() == 220070000) {
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);
				if (var == 3) {
					playQuestMovie(env, 551);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 9) {
				if (player.isInsideZone(ZoneName.get("DF4_ITEMUSEAREA_Q20031_220070000"))) {

					return HandlerResult.fromBoolean(useQuestItem(env, item, 9, 10, false, 566));
					// playQuestMovie(env, 566);
					// return HandlerResult.fromBoolean(useQuestItem(env, item, 9, 10, false)); // 10
				}
			}
		}
		return HandlerResult.FAILED;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 551)
			changeQuestStep(env, 3, 4);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player);
	}
}
