package quest.altgard;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Pyro refix by Nephis Aller tuer 4 brutes et retourner voir Meiyer Status locked de toutes les missions de Altgard
 */
public class _2012Encroachers extends QuestHandler {

	private final static int questId = 2012;
	private final static int[] mob_ids = { 210715 }; // Brute lvl 10

	public _2012Encroachers() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203559).addOnTalkEvent(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		for (int mob_id : mob_ids)
			qe.registerQuestNpc(mob_id).addOnKillEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env, 2011);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2200, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		/** Initialisation de l'event **/
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		/** Si on start la quete **/
		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 203559) {

				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0) // Initialisation du dialogue
							return sendQuestDialog(env, 1011);
						else if (var <= 5) // Rendu de la quete
						{
							return sendQuestDialog(env, 1352);
						} else if (var >= 5) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
						}
					case SETPRO1:
					case SETPRO2:
						if (var == 0 || var == 5) {
							qs.setQuestVarById(0, var + 1);
							updateQuestStatus(env);
							return sendQuestSelectionDialog(env);
						}
				}

			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203559) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		/** Checks **/
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() != QuestStatus.START)
			return false;

		switch (targetId) {
			case 210715: // Brute
				if (var > 0 && var < 4) // En tuer 4
				{
					qs.setQuestVarById(0, var + 1);
					updateQuestStatus(env);
					return true;
				} else if (var == 4) // Au 4eme REWARD
				{
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					return true;
				}
		}
		return false;
	}

}

/** FIN MODIF EVO **/
