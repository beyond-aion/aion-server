package quest.heiron;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;

/**
 * @author Balthazar, vlog
 */
public class _1647DressingUpForBollvig extends AbstractQuestHandler {

	public _1647DressingUpForBollvig() {
		super(1647);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(790019).addOnQuestStart(questId);
		qe.registerQuestNpc(790019).addOnTalkEvent(questId);
		qe.registerQuestNpc(700272).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 790019) { // Zetus
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 4762);
					default: {
						return sendQuestStartDialog(env, 182201783, 1);
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 700272) { // Suspicious Stone Statue
				if (dialogActionId == USE_OBJECT) {
					// Wearing Stenon Blouse and Stenon Skirt
					if (!player.getEquipment().getEquippedItemsByItemId(110100150).isEmpty()
						&& !player.getEquipment().getEquippedItemsByItemId(113100144).isEmpty()) {
						// Having Myanee's Flute
						if (player.getInventory().getItemCountByItemId(182201783) > 0) {
							playQuestMovie(env, 199);
							return useQuestObject(env, 0, 0, true, false); // reward
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 790019) { // Zetus
				switch (dialogActionId) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 10002);
					default: {
						return sendQuestEndDialog(env);
					}
				}
			}
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 199) {
			Player player = env.getPlayer();
			spawnForFiveMinutesInFrontOf(204635, player, 2);
			spawnForFiveMinutes(204635, player.getWorldMapInstance(), player.getX() + 2, player.getY() - 2, player.getZ(), (byte) 0);
			spawnForFiveMinutes(204635, player.getWorldMapInstance(), player.getX() - 2, player.getY() + 2, player.getZ(), (byte) 0);
		}
	}

}
