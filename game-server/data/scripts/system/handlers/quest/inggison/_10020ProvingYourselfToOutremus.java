package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Speak to Outremus (798926). Speak to Brigade General Yulia (798928). Repair the broken Obelisks near Inggison Illusion Fortress: Repair the Stopped
 * Obelisk (730223), Repair the Overheated Obelisk (730224), Repair the Deteriorated Obelisk (730225). Deal with the Balaur who are damaging the
 * Obelisks: Hikiron Farm Balaur (10): Basrasa Laborer (215504, 216782, 215505), Basrasa Collector (216463, 216783, 216464), Basrasa Ambusher (216692,
 * 215517, 216648, 215519), Basrasa Assaulter (216691, 215516, 216647, 215518). Armored Spallers (215508, 215509) (2). Speak to Brigade General Yulia.
 * Speak to Brigade General Versetti (798927). Speak to Centurion Marica (798955). Install the Obelisk at the Eastern Obelisk Support (700628) of the
 * Dimaia Fountainhead. Install the Obelisk at the Western Obelisk Support (700629) of the Dimaia Fountainhead. Install the Obelisk at the Northern
 * Obelisk Support (700630) of the Dimaia Fountainhead. Report to Brigade General Versetti. Report the result to Outremus.
 * 
 * @author vlog
 */
public class _10020ProvingYourselfToOutremus extends QuestHandler {

	private final static int questId = 10020;

	public _10020ProvingYourselfToOutremus() {
		super(questId);
	}

	@Override
	public void register() {
		int[] npcs = { 798926, 798928, 730223, 730224, 730225, 798927, 798955, 700628, 700629, 700630 };
		int[] mobs = { 215504, 216782, 215505, 216463, 216783, 216464, 216692, 215517, 216648, 215519, 216691, 215516, 216647, 215518, 215508, 215509 };
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 10000, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int var1 = qs.getQuestVarById(1);
		int var2 = qs.getQuestVarById(2);
		int var3 = qs.getQuestVarById(3);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();
		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 798926: { // Outremus
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 0) {
								return sendQuestDialog(env, 1011);
							}
						}
						case SETPRO1: {
							return defaultCloseDialog(env, 0, 1); // 1
						}
					}
					break;
				}
				case 798928: { // Yulia
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 1) {
								return sendQuestDialog(env, 1352);
							} else if (var == 4) {
								return sendQuestDialog(env, 2375);
							}
						}
						case SETPRO2: {
							return defaultCloseDialog(env, 1, 2, 182206301, 1, 0, 0); // 2
						}
						case SETPRO5: {
							return defaultCloseDialog(env, 4, 5); // 5
						}
					}
					break;
				}
				case 730223: { // Stopped Obelisk
					switch (dialog) {
						case USE_OBJECT: {
							if (var == 2 && var1 == 0) {
								if (var2 == 1 && var3 == 1) {
									qs.setQuestVar(3); // 3
									updateQuestStatus(env);
									return true;
								} else {
									changeQuestStep(env, 0, 1, false, 1); // 1: 1
									return true;
								}
							}
						}
					}
					break;
				}
				case 730224: { // Overheated Obelisk
					switch (dialog) {
						case USE_OBJECT: {
							if (var == 2 && var2 == 0) {
								if (var1 == 1 && var3 == 1) {
									qs.setQuestVar(3); // 3
									updateQuestStatus(env);
									return true;
								} else {
									changeQuestStep(env, 0, 1, false, 2); // 2: 1
									return true;
								}
							}
						}
					}
					break;
				}
				case 730225: { // Deteriorated Obelisk
					switch (dialog) {
						case USE_OBJECT: {
							if (var == 2 && var3 == 0) {
								if (var2 == 1 && var1 == 1) {
									qs.setQuestVar(3); // 3
									updateQuestStatus(env);
									return true;
								} else {
									changeQuestStep(env, 0, 1, false, 3); // 3: 1
									return true;
								}
							}
						}
					}
					break;
				}
				case 798927: { // Versetti
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 5) {
								return sendQuestDialog(env, 2716);
							} else if (var == 10) {
								return sendQuestDialog(env, 3398);
							}
						}
						case SETPRO6: {
							return defaultCloseDialog(env, 5, 6); // 6
						}
						case SET_SUCCEED: {
							return defaultCloseDialog(env, 10, 10, true, false, 0, 0, 182206301, 1); // reward
						}
					}
					break;
				}
				case 798955: { // Marica
					switch (dialog) {
						case QUEST_SELECT: {
							if (var == 6) {
								return sendQuestDialog(env, 3057);
							}
						}
						case SETPRO7: {
							return defaultCloseDialog(env, 6, 7); // 7
						}
					}
					break;
				}
				case 700628: { // Eastern Obelisk Support
					if (dialog == DialogAction.USE_OBJECT) {
						if (var == 7) {
							return useQuestObject(env, 7, 8, false, 0); // 8
						}
					}
					break;
				}
				case 700629: { // Western Obelisk Support
					if (dialog == DialogAction.USE_OBJECT) {
						if (var == 8) {
							return useQuestObject(env, 8, 9, false, 0); // 9
						}
					}
					break;
				}
				case 700630: { // Northern Obelisk Support
					if (dialog == DialogAction.USE_OBJECT) {
						if (var == 9) {
							return useQuestObject(env, 9, 10, false, 0); // 10
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798926) { // Outremus
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialog() == DialogAction.SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
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
		int targetId = env.getTargetId();
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int var1 = qs.getQuestVarById(1);
		int var2 = qs.getQuestVarById(2);
		if (qs.getStatus() == QuestStatus.START) {
			if (var == 3) {
				int[] mobs = { 215504, 216782, 215505, 216463, 216783, 216464, 216692, 215517, 216648, 215519, 216691, 215516, 216647, 215518 };
				int[] spellers = { 215508, 215509 };
				if (targetId == spellers[0] || targetId == spellers[1]) {
					if (var2 == 0) {
						return defaultOnKillEvent(env, spellers, var2, var2 + 1, 2); // 2: 0-1
					} else if (var2 == 1) {
						if (var1 == 10) {
							qs.setQuestVar(4); // 4
							updateQuestStatus(env);
							return true;
						} else {
							return defaultOnKillEvent(env, spellers, 1, 2, 2); // 2: 2
						}
					}
				} else {
					if (var1 < 9) {
						return defaultOnKillEvent(env, mobs, var1, var1 + 1, 1); // 1: 0 - 9
					} else if (var1 == 9) {
						if (var2 == 2) {
							qs.setQuestVar(4); // 4
							updateQuestStatus(env);
							return true;
						} else {
							return defaultOnKillEvent(env, mobs, 9, 10, 1); // 1: 9
						}
					}
				}
			}
		}
		return false;
	}
}
