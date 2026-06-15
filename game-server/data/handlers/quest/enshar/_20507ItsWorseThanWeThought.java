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
 * 							Talk with Xard.
 *               Talk with Theodoricus.
 *               Talk with Daruku.
 *               Look for Bakorn at the entrance of the Uncharted Cave.
 *               Investigate the Uncharted Cave.
 *               Report to Xard.
 *               Order: Governor Xard is looking for you. Go and see him.
 */
public class _20507ItsWorseThanWeThought extends AbstractQuestHandler {

	public _20507ItsWorseThanWeThought() {
		super(20507);
	}

	@Override
	public void register() {
		// Xard 804738
		// Theodoricus 804739
		// Daruku 804740
		// Bakorn 804741
		int[] npcs = { 804738, 804739, 804740, 804741 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerOnEnterZone(ZoneName.get("DF5_SENSORYAREA_Q20507A_206377_9_220080000"), questId); // Uncharted Cave zone
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
			case 804738: // Xard
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Xard.
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
			case 804739: // Theodoricus
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Talk with Theodoricus.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1352);

						if (dialogActionId == SETPRO2)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 804740: // Daruku
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 2) { // Step 2: Talk with Daruku.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1693);

						if (dialogActionId == SETPRO3)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 804741: // Bakorn
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 3) { // Step 3: Talk with Bakorn.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 2034);

						if (dialogActionId == SETPRO4)
							return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {

		if (zoneName == ZoneName.get("DF5_SENSORYAREA_Q20507A_206377_9_220080000")) {

			Player player = env.getPlayer();
			if (player == null) {
				return false;
			}

			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);

				if (var == 4) { // Step 4: Investigate the Uncharted Cave.
					qs.setStatus(QuestStatus.REWARD);
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
					playQuestMovie(env, 864);
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
		if (movieId != 864)
			return;
		Player player = env.getPlayer();
		spawnForFiveMinutes(219954, player.getWorldMapInstance(), player.getX() + 3, player.getY() + 1, player.getZ(), (byte) 60);
		spawnForFiveMinutes(219955, player.getWorldMapInstance(), player.getX() + 2, player.getY() + 3, player.getZ(), (byte) 73);
	}
}
