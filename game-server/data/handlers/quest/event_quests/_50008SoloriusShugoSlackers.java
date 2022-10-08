package quest.event_quests;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnSearchResult;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PositionUtil;

/**
 * @author Cheatkiller
 */
public class _50008SoloriusShugoSlackers extends AbstractQuestHandler {

	public _50008SoloriusShugoSlackers() {
		super(50008);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(831038).addOnQuestStart(questId);
		qe.registerQuestNpc(831038).addOnTalkEvent(questId);
		qe.registerQuestNpc(219290).addOnAttackEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 831038) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 831038) {
				if (dialogActionId == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onAttackEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int targetId = env.getTargetId();
			int var = qs.getQuestVarById(0);
			if (targetId == 219290) {
				final Npc npc = (Npc) env.getVisibleObject();
				if (!npc.isSpawned())
					return false;
				final SpawnSearchResult searchResult = DataManager.SPAWNS_DATA.getFirstSpawnByNpcId(npc.getWorldId(), 831036);
				if (PositionUtil.getDistance(searchResult.getSpot().getX(), searchResult.getSpot().getY(), searchResult.getSpot().getZ(), npc.getX(), npc.getY(),
					npc.getZ()) <= 15) {
					npc.getController().deleteAndScheduleRespawn();
					if (var == 0)
						changeQuestStep(env, 0, 1);
					else
						changeQuestStep(env, 1, 1, true);
					return true;
				}
			}
		}
		return false;
	}
}
