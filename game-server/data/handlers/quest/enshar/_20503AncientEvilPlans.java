package quest.enshar;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @Author Majka
 * @Description:
 * 							Talk with Engrid.
 *               Talk with Egzen.
 *               Kill Aetheric Protection Dominators to get information and return to Egzen.
 *               Go to the Aetheric Field Stone Findspot.
 *               Talk with Engrid.
 *               Order: Engrid is looking for you. Go see him.
 */
public class _20503AncientEvilPlans extends AbstractQuestHandler {

	public _20503AncientEvilPlans() {
		super(20503);
	}

	@Override
	public void register() {
		// Engrid 804728
		// Egzen 805221
		int[] npcs = { 804728, 804729 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnEnterZone(ZoneName.get("DF5_SENSORYAREA_Q20503A_206394_8_220080000"), questId); // Aetheric Field Stone Findspot zone
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		int dialogActionId = env.getDialogActionId();

		switch (targetId) {
			case 804728:
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Engrid.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialogActionId == SETPRO1)
							return defaultCloseDialog(env, var, var + 1);
					}
				}

				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}

					return sendQuestEndDialog(env);
				}
				break;
			case 804729: // Egzen
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Talk with Egzen.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SETPRO2)
							return defaultCloseDialog(env, var, var + 1);
					}

					if (var == 2) { // Step 2: Kill Aetheric Protection Dominators to get information and return to Egzen.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1693);
						if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
							long itemCount = player.getInventory().getItemCountByItemId(182215640); // Aetheric Token
							if (itemCount >= 1) {
								removeQuestItem(env, 182215640, itemCount);
								qs.setQuestVar(var + 1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
					}
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {

		if (zoneName == ZoneName.get("DF5_SENSORYAREA_Q20503A_206394_8_220080000")) {

			Player player = env.getPlayer();
			if (player == null) {
				return false;
			}

			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);

				if (var == 3) { // Step 3: Go to the Aetheric Field Stone Findspot.
					qs.setStatus(QuestStatus.REWARD);
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
					playQuestMovie(env, 862);
					player.getMoveController().abortMove();
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 20500);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 20500);
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId != 862)
			return;
		Player player = env.getPlayer();
		spawnTemporarily(805221, player.getWorldMapInstance(), player.getX(), player.getY() + 3, player.getZ(), (byte) 0, 2); // Egzen
		spawnTemporarily(804859, player.getWorldMapInstance(), player.getX() + 1, player.getY() + 1, player.getZ(), (byte) 20, 2); // Archon Shadowthrash
		spawnTemporarily(804860, player.getWorldMapInstance(), player.getX() + 3, player.getY() + 1, player.getZ(), (byte) 60, 2); // Archon Shadowthrash
		spawnTemporarily(804861, player.getWorldMapInstance(), player.getX() + 2, player.getY() + 3, player.getZ(), (byte) 73, 2); // Archon Shadowthrash
	}
}
