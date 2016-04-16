package quest.altgard;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author Ritsu
 * @Modified Majka
 */
public class _2252ChasingtheLegend extends QuestHandler {

	private final static int questId = 2252;
	private final static int questStartNpcId = 203646; // Sinood
	private final static int questStep1NpcId = 700060; // Bones of Munishan (Npc)
	private final static int questActionItemId = 182203235; // Bones of Munishan (Item)
	private final static int questKillNpc1Id = 210634; // Minushan's Spirit
	private final static int questKillNpc2Id = 210635; // Minushan Drakie. To check on retail if it is spawned also 210635

	public _2252ChasingtheLegend() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(questStartNpcId).addOnQuestStart(questId); // Sinood
		qe.registerQuestNpc(questStartNpcId).addOnTalkEvent(questId);
		qe.registerQuestNpc(questStep1NpcId).addOnTalkEvent(questId); // Bone of Minusha
		qe.registerQuestNpc(questKillNpc1Id).addOnKillEvent(questId); // Minushan's Spirit
		qe.registerQuestNpc(questKillNpc2Id).addOnKillEvent(questId); // Minushan Drakie
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == questStartNpcId) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (dialog == DialogAction.QUEST_ACCEPT_1) {
					if (QuestService.startQuest(env)) {
						giveQuestItem(env, questActionItemId, 1);
						return sendQuestDialog(env, 1003);
					}
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == questStartNpcId) {
				switch (dialog) {
					case QUEST_SELECT:
						if (var == 0) {
							long MinushanBone = player.getInventory().getItemCountByItemId(questActionItemId);
							if (MinushanBone == 0) { // Player hasn't action item; dialogue for a new chance
								giveQuestItem(env, questActionItemId, 1);
								qs.setQuestVarById(0, 0);
								return sendQuestDialog(env, 1693);
							} else { // Dialogue for encouragement
								return sendQuestDialog(env, 2034);
							}
						}
				}
			}
			if (targetId == questStep1NpcId) {
				switch (dialog) {
					case USE_OBJECT:
						final Npc npc = (Npc) player.getTarget();

						if (npc == null || npc.getIsQuestBusy()) {
							return false;
						}

						if (var == 0 && checkItemExistence(env, questActionItemId, 1, true)) {
							npc.setIsQuestBusy(true); // Set npc not usable during spawn time

							// Random spawn
							int chance = 95; // Chance to spawn biggest reward mob
							int spawnTime = 3; // 3 min of spawn
							int questSpawnedNpcId = (Rnd.get(1, 100) <= chance ? questKillNpc1Id : questKillNpc2Id);
							final Npc questMob = (Npc) QuestService.spawnQuestNpc(player.getWorldId(), player.getInstanceId(), questSpawnedNpcId, npc.getX(), npc.getY(), npc.getZ(), npc.getHeading()); // Minushan's Spirit or Minushan's Drakie
							PacketSendUtility.broadcastMessage(questMob, 1100630);

							// @ToDo: setting not usable icon while mob is spawned
							ThreadPoolManager.getInstance().schedule(new Runnable() {

								@Override
								public void run() {
									npc.setIsQuestBusy(false);
									questMob.getController().delete();
									// @ToDo: setting usable icon after mob is despawned
								}
							}, spawnTime * 60000);
						}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			int var = qs.getQuestVarById(0);
			
			switch (dialog) {
				case SELECT_QUEST_REWARD:
				case SELECTED_QUEST_NOREWARD:
					int reward = var - 1;
					return sendQuestEndDialog(env, reward);
				default:
					return sendQuestDialog(env, 1352);
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

		int var = qs.getQuestVarById(0);
		if (var == 0) {
			int targetId = env.getTargetId();
			switch (targetId) {
				case questKillNpc1Id: // Minushan's Spirit - Highest reward
					qs.setStatus(QuestStatus.REWARD);
					qs.setQuestVarById(0, 1);
					updateQuestStatus(env);
					return true;
				case questKillNpc2Id: // Minushan Drakie - Lowest reward
					qs.setStatus(QuestStatus.REWARD);
					qs.setQuestVarById(0, 2);
					updateQuestStatus(env);
					return true;
			}
		}
		return false;
	}
}
