package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @Author Majka
 */
public class _24112NoLaissezFaireForLepharists extends AbstractQuestHandler {

	private final static int questStartNpcId = 203631; // Nokir
	private final static int questEndNpcId = 832821; // Brodir
	private final static int questKillNpcId = 210510; // Comrade Sumarhon

	public _24112NoLaissezFaireForLepharists() {
		super(24112);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(questStartNpcId).addOnQuestStart(questId);
		qe.registerQuestNpc(questStartNpcId).addOnTalkEvent(questId);
		qe.registerQuestNpc(questEndNpcId).addOnTalkEvent(questId);
		qe.registerQuestNpc(questKillNpcId).addOnKillEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) {
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (targetId == questStartNpcId) { // Nokir
			if (qs == null || qs.isStartable()) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 1011);
					default:
						return sendQuestStartDialog(env);
				}
			}
		}

		if (targetId == questEndNpcId) { // Brodir

			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);

				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0) { // @ToDo: find a way to reach this part. It works with var = -1 but doesn't activate following step
							return sendQuestDialog(env, 1352);
						} else if (var == 1) {
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SETPRO1:
						qs.setQuestVarById(0, 1);
						return sendQuestSelectionDialog(env);
					case SELECT_QUEST_REWARD:
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						return sendQuestEndDialog(env);
				}
			}

			if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START) {
			return false;
		}

		int targetId = env.getTargetId();
		int var = qs.getQuestVarById(0);

		if (var == 0 && targetId == questKillNpcId) {
			qs.setQuestVarById(0, 1);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}
}
