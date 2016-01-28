package quest.gelkmaros;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @Author Majka
 */
public class _20035SilenteraSupport extends QuestHandler {

	private final static int questId = 20035;
	// Richelle					ID: 799225
	// Valetta					ID: 799226
	// Jecasti					ID: 799283
	// Arango						ID: 799309
	// Mastarius				ID: 799323
	// Notud						ID: 799329
	private final static int[] npcs = {799225, 799226, 799283, 799309, 799323, 799329};
	private final static int[] mobs = {216101, 216104, 216107, 216108, 216109, 216112, 216448, 216449, 216450, 216451};

	public _20035SilenteraSupport() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerQuestItem(182215659, questId);
		qe.registerQuestItem(182215660, questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 799226: { // Valetta
					switch (dialog) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							} else if (var == 7) {
								return sendQuestDialog(env, 3398);
							}
							break;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1); // 1
						case SET_SUCCEED:
							removeQuestItem(env, 182215660, 1);
							qs.setStatus(QuestStatus.REWARD);
							return defaultCloseDialog(env, 7, 8); //reward
					}
					break;
				}
				case 799329: { // Notud
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2); // 2
						}

					}
					break;
				}
				case 799323: { // Mastarius
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
						}
						case SETPRO3: {
							giveQuestItem(env, 182215596, 1);
							giveQuestItem(env, 182215597, 1);
							return defaultCloseDialog(env, 2, 3); // 3
						}
					}
					break;
				}
				case 799283: { // Jecasti
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
						}
						case SETPRO4: {
							removeQuestItem(env, 182215596, 1);
							return defaultCloseDialog(env, 3, 4); // 4
						}
					}
					break;
				}
				case 799309: { // Arango
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SELECT_ACTION_2376: {
							playQuestMovie(env, 567);
							return sendQuestDialog(env, 2376);
						}
						case SETPRO5: {
							removeQuestItem(env, 182215597, 1);
							giveQuestItem(env, 182215659, 1);
							return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799225) { // Richelle
				if (dialog == DialogAction.USE_OBJECT) {
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
			if (var == 5) {
				int var1 = qs.getQuestVarById(1);
				if (var1 >= 0 && var1 < 9) {
					return defaultOnKillEvent(env, mobs, var1, var1 + 1, 1);
				} else if (var1 == 9) {
					qs.setQuestVar(6); //6
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		if (player.isInsideZone(ZoneName.get("DF4_ITEMUSEAREA_Q20035_220070000"))) {
			return HandlerResult.fromBoolean(useQuestItem(env, item, 6, 7, false, 182215660, 1)); // 7
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}
}
