package quest.levinshor;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Pad
 */
public class _13745EljersRequest extends AbstractQuestHandler {

	private static final int npcId = 802350;
	private static final int worldId = 600100000;

	public _13745EljersRequest() {
		super(13745);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(npcId).addOnTalkEvent(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnKillInWorld(worldId, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int targetId = env.getTargetId();

		if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == npcId) {
				switch (env.getDialogActionId()) {
					case USE_OBJECT:
						return sendQuestDialog(env, 10002);
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
					default:
						return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (player.getWorldId() == worldId && (qs == null || qs.isStartable()))
			return QuestService.startQuest(env);
		return false;
	}

	@Override
	public boolean onKillInWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs == null || qs.getStatus() != QuestStatus.START || qs.getQuestVarById(0) != 0)
			return false;

		if (!(env.getVisibleObject() instanceof Player))
			return false;
		Player target = (Player) env.getVisibleObject();

		if (player.getLevel() >= target.getLevel() - 5 && player.getLevel() <= target.getLevel() + 9) {
			if (qs.getQuestVarById(1) == 0) {
				qs.setQuestVarById(1, 1);
			} else {
				qs.setQuestVarById(0, 1);
				qs.setStatus(QuestStatus.REWARD);
			}
			updateQuestStatus(env);
			return true;
		}

		return false;
	}
}
