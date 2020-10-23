package quest.cygnea;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @Author Majka
 * @Description:
 * 							Talk with Mosphera at the Aequis First Hold.
 *               Use the Ruins Location Map to find the first location.
 *               Examine the Collapsed Ruins.
 *               Use the Ruins Location Map to find the second location.
 *               Examine the Ancient Balaur Corpse.
 *               Use the Ruins Location Map to find the third location.
 *               Obtain the Token of Balaur and take it to Mosphera.
 *               Help Mosphera investigate the secret of the suspicious ruins.
 */
public class _10501ResearchtheRuins extends AbstractQuestHandler {

	private final static int workItemId = 182215598; // Ruins Location Map

	public _10501ResearchtheRuins() {
		super(10501);
	}

	@Override
	public void register() {
		// Mosphera 804700
		// Ancient Balaur Corpse 731535
		// Collapsed Ruins 731536
		int[] npcs = { 804700, 731535, 731536 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestItem(workItemId, questId);
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
			case 804700: // Mosphera
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Mosphera at the Aequis First Hold.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 1011);

						if (dialogActionId == SETPRO1)
							return defaultCloseDialog(env, var, var + 1, workItemId, 1); // Gives item Ruins Location Map [ID: 182215598]
					}

					if (var == 6) { // Step 6: Obtain the Token of Balaur and take it to Mosphera.
						if (dialogActionId == QUEST_SELECT)
							return sendQuestDialog(env, 3057);

						// @ToCheck: is Cygnea Aetheric Field Stone [ID: 702760] related with mission?
						if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
							long itemCount = player.getInventory().getItemCountByItemId(182215599);
							if (itemCount > 0) {
								removeQuestItem(env, 182215599, itemCount);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
					}
				}
				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialogActionId == USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}

					return sendQuestEndDialog(env);
				}
				break;
			case 731535: // Ancient Balaur Corpse
				if (var == 4) { // Step 4: Examine the Ancient Balaur Corpse.
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 2375);
					if (dialogActionId == SETPRO5) {
						// Spawn Beritra Raider for 10 minutes
						spawnTemporarily(236250, player.getWorldMapInstance(), 2067.6863f, 386.52222f, 565.7099f, (byte) 70, 10);
						return defaultCloseDialog(env, var, var + 1);
					}
				}
				break;
			case 731536: // Collapsed Ruins
				if (var == 2) { // Step 2: Examine the Collapsed Ruins
					if (dialogActionId == QUEST_SELECT)
						return sendQuestDialog(env, 1693);
					if (dialogActionId == SETPRO3)
						return defaultCloseDialog(env, var, var + 1);
				}
				break;
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return HandlerResult.UNKNOWN;

		final int id = item.getItemTemplate().getTemplateId();
		if (id != workItemId)
			return HandlerResult.UNKNOWN;

		final int itemObjId = item.getObjectId();
		PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 1000, 0, 0), true);
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
				int var = qs.getQuestVarById(0);
				// Step 1: Use the Ruins Location Map to find the first location
				// Step 3: Use the Ruins Location Map to find the second location
				// Step 5: Use the Ruins Location Map to find the third location
				if (var == 1 || var == 3 || var == 5) {
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
				}
			}
		}, 1000);
		return HandlerResult.SUCCESS;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		defaultOnQuestCompletedEvent(env, 10500);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		defaultOnLevelChangedEvent(player, 10500);
	}
}
