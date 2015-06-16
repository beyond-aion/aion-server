package quest.poeta;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author MrPoke
 */
public class _1123WheresTutty extends QuestHandler {

	private final static int questId = 1123;

	public _1123WheresTutty() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(790001).addOnTalkEvent(questId);
		qe.registerQuestNpc(790001).addOnQuestStart(questId);
		qe.registerOnEnterZone(ZoneName.get("QUEST_1123_210010000"), questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 790001) {
			if (qs == null || qs.getStatus() == QuestStatus.NONE) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
			else if (qs.getStatus() == QuestStatus.REWARD) {
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 1352);
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName != ZoneName.get("QUEST_1123_210010000"))
			return false;
		final Player player = env.getPlayer();
		if (player == null)
			return false;
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		env.setQuestId(questId);
		playQuestMovie(env, 11);
		qs.setStatus(QuestStatus.REWARD);
		updateQuestStatus(env);
		return true;
	}
}
