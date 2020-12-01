package quest.inggison;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @Author Majka
 */
public class _10035SoartotheCorridor extends AbstractQuestHandler {

	private final static int[] mobs = { 220021, 220022, 216775 };

	public _10035SoartotheCorridor() {
		super(10035);
	}

	@Override
	public void register() {
		// Yulia ID: 798928
		// Laokon ID: 799025
		// Veille ID: 798958
		// Laetia ID: 798996
		// Corridor Entry Controller ID: 702663
		// Outremus ID: 798926
		int[] npcs = { 798928, 799025, 798958, 798996, 206363, 702663, 798926 };
		qe.registerOnLevelChanged(questId);
		qe.registerOnQuestCompleted(questId);
		qe.registerOnEnterZone(ZoneName.get("ANGRIEF_GATE_210050000"), questId);
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
		int dialogActionId = env.getDialogActionId();
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798928: // Yulia
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							} else if (var == 7) {
								return sendQuestDialog(env, 3398);
							}
							break;
						case SETPRO1:
							return defaultCloseDialog(env, 0, 1);
						case SET_SUCCEED:
							qs.setQuestVar(8);
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return closeDialogWindow(env);
					}
					break;
				case 799025: // Laokon
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							}
							break;
						case SETPRO2:
							return defaultCloseDialog(env, 1, 2);
					}
					break;
				case 798958: // Veille
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 2) {
								return sendQuestDialog(env, 1693);
							}
							break;
						case SETPRO3:
							return defaultCloseDialog(env, 2, 3);
					}
					break;
				case 798996: // Laetia
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (var == 3) {
								return sendQuestDialog(env, 2034);
							}
							break;
						case SELECT4_1:
							playQuestMovie(env, 517);
							return sendQuestDialog(env, 2035);
						case SETPRO4:
							giveQuestItem(env, 182215629, 1);
							return defaultCloseDialog(env, 3, 4);
					}
					break;
				case 702663: // Corridor Entry Controller
					switch (dialogActionId) {
						case USE_OBJECT:
							if (var == 6) {
								removeQuestItem(env, 182215629, 1);
								// spawnForFiveMinutes(700641, player, 1377, 2297, 296, (byte) 90); ToDo: to check on retail what's
								// its function
								qs.setQuestVar(7);
								updateQuestStatus(env);
								return true;
							}
							break;
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798926) { // Outremus
				if (env.getDialogActionId() == USE_OBJECT) {
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
					qs.setQuestVar(6);
					updateQuestStatus(env);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (zoneName.equals(ZoneName.get("ANGRIEF_GATE_210050000"))) {
				if (var == 4) {
					changeQuestStep(env, 4, 5);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 10031, 10032, 10033, 10034);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 10031, 10032, 10033, 10034);
	}
}
