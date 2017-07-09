package quest.eltnen;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Atomics
 */
public class _1376AMountaineOfTrouble extends AbstractQuestHandler {

	public _1376AMountaineOfTrouble() {
		super(1376);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203947).addOnQuestStart(questId); // Beramones
		qe.registerQuestNpc(203947).addOnTalkEvent(questId); // Beramones
		qe.registerQuestNpc(203964).addOnTalkEvent(questId); // Agrips
		qe.registerQuestNpc(210976).addOnKillEvent(questId); // Kerubien Hunter
		qe.registerQuestNpc(210986).addOnKillEvent(questId); // Kerubien Hunter
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;

		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 203947) { // Beramones
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203964) { // Agrips
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 1352);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		int[] mobs = { 210976, 210986 };
		if (defaultOnKillEvent(env, mobs, 0, 6) || defaultOnKillEvent(env, mobs, 6, true))
			return true;
		else
			return false;
	}
}
