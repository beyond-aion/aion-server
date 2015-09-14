package quest.hidden_truth;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * Talk with Pernos (790001). Accept the blessing of Daminu (730008). Accept the blessing of Lodas (730019). Accept the blessing of Arbolu (204647).
 * Accept the energy of Khidia (203183). Accept the energy of Tumblusen (203989). Accept the energy of Atropos (798155). Accept the energy of Aphesius
 * (204549). Accept the energy of Jucleas (203752). Accept the blessing of Morai (203164). Accept the blessing of Gaia (203917). Accept the blessing
 * of Kimeia (203996). Accept the blessing of Jamanok (798176). Accept the blessing of Serimnir (798212). Accept the blessing of Maximus (204535).
 * Take the marble to Pernos in Poeta.
 * 
 * @author Manu72
 * @reworked vlog
 */
public class _1098PearlofProtection extends QuestHandler {

	private final static int questId = 1098;
	private final static int[] npcs = { 790001, 730008, 730019, 204647, 203183, 203989, 798155, 204549, 203752, 203164, 203917, 203996, 798176, 798212,
		204535 };

	public _1098PearlofProtection() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 1097);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);

		if (qs != null && qs.getStatus() == QuestStatus.REWARD) { // Reward
			if (targetId == 790001) { // Pernos
				if (env.getDialog() == DialogAction.QUEST_SELECT) {
					removeQuestItem(env, 182206065, 1);
					return sendQuestDialog(env, 10002);
				} else
					return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 790001) { // Pernos
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 0)
					return sendQuestDialog(env, 1011);
				if (env.getDialog() == DialogAction.SETPRO1) {
					return defaultCloseDialog(env, 0, 1, 182206062, 1, 0, 0); // 1
				}
			} else if (targetId == 730008) { // Daminu
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 1)
					return sendQuestDialog(env, 1352);
				if (env.getDialog() == DialogAction.SETPRO2) {
					return defaultCloseDialog(env, 1, 2); // 2
				}
			} else if (targetId == 730019) { // Lodas
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 2)
					return sendQuestDialog(env, 1693);
				if (env.getDialog() == DialogAction.SETPRO3) {
					return defaultCloseDialog(env, 2, 3); // 3
				}
			} else if (targetId == 204647) { // Voice of Arbolu
				if ((env.getDialog() == DialogAction.QUEST_SELECT || env.getDialog() == DialogAction.USE_OBJECT) && var == 3)
					return sendQuestDialog(env, 2034);
				else if (env.getDialog() == DialogAction.SETPRO4) {
					return defaultCloseDialog(env, 3, 4, 182206063, 1, 182206062, 1); // 4
				}
			} else if (targetId == 203183) { // Khidia
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 4)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogId() == DialogAction.SETPRO5.id()) {
					return defaultCloseDialog(env, 4, 5); // 5
				}
			} else if (targetId == 203989) { // Tumblusen
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 5)
					return sendQuestDialog(env, 2716);
				else if (env.getDialogId() == DialogAction.SETPRO6.id()) {
					return defaultCloseDialog(env, 5, 6); // 6
				}
			} else if (targetId == 798155) { // Atropos
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 6)
					return sendQuestDialog(env, 3057);
				else if (env.getDialogId() == DialogAction.SETPRO7.id()) {
					return defaultCloseDialog(env, 6, 7); // 7
				}
			} else if (targetId == 204549) { // Aphesius
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 7)
					return sendQuestDialog(env, 3398);
				else if (env.getDialogId() == DialogAction.SETPRO8.id()) {
					return defaultCloseDialog(env, 7, 8); // 8
				}
			} else if (targetId == 203752) { // Jucleas
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 8)
					return sendQuestDialog(env, 3739);
				else if (env.getDialogId() == DialogAction.SETPRO9.id()) {
					return defaultCloseDialog(env, 8, 9, 182206064, 1, 182206063, 1); // 9
				}
			} else if (targetId == 203164) { // Morai
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 9)
					return sendQuestDialog(env, 4080);
				else if (env.getDialogId() == DialogAction.SETPRO10.id()) {
					return defaultCloseDialog(env, 9, 10); // 10
				}
			} else if (targetId == 203917) { // Gaia
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 10)
					return sendQuestDialog(env, 1608);
				else if (env.getDialogId() == DialogAction.SETPRO11.id()) {
					return defaultCloseDialog(env, 10, 11); // 11
				}
			} else if (targetId == 203996) { // Kimeia
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 11)
					return sendQuestDialog(env, 1949);
				else if (env.getDialogId() == DialogAction.SETPRO12.id()) {
					return defaultCloseDialog(env, 11, 12); // 12
				}
			} else if (targetId == 798176) { // Jamanok
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 12)
					return sendQuestDialog(env, 2290);
				else if (env.getDialogId() == DialogAction.SETPRO13.id()) {
					return defaultCloseDialog(env, 12, 13); // 13
				}
			} else if (targetId == 798212) { // Serimnir
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 13)
					return sendQuestDialog(env, 2631);
				else if (env.getDialogId() == DialogAction.SETPRO14.id()) {
					return defaultCloseDialog(env, 13, 14); // 14
				}
			} else if (targetId == 204535) { // Maximus
				if (env.getDialog() == DialogAction.QUEST_SELECT && var == 14)
					return sendQuestDialog(env, 2972);
				else if (env.getDialogId() == DialogAction.SET_SUCCEED.id()) {
					return defaultCloseDialog(env, 14, 14, true, false, 182206065, 1, 182206064, 1); // reward
				}
			}
		}
		return false;
	}
}
