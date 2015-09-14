package quest.tiamaranta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author mr.madison
 */
public class _41506ScoutingforWeakness extends QuestHandler {

	private final static int questId = 41506;

	public _41506ScoutingforWeakness() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(205934).addOnQuestStart(questId);
		qe.registerQuestNpc(205934).addOnTalkEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("BALAUR_CAVALRY_BASE_600030000"), questId);
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName != ZoneName.get("BALAUR_CAVALRY_BASE_600030000"))
			return false;
		Player player = env.getPlayer();
		if (player == null)
			return false;
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getQuestVars().getQuestVars() != 0)
			return false;
		if (qs.getStatus() != QuestStatus.START)
			return false;
		env.setQuestId(questId);
		qs.setStatus(QuestStatus.REWARD);
		updateQuestStatus(env);
		return true;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 205934) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205934) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

}
