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
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @Author Majka
 * @Description
 * 							Talk with Averse at Erivale Territory Village.
 *              Talk with Investigator Erinos who has been dispatched to Twilight Temple.
 *              Gather the Balaur Maneuver Plans and talk with Erinos.
 *              Talk with Erinos.
 *              Read the Restored Balaur Maneuver Plans and find out what Beritra is up to.
 *              Go to Erivale Territory Village and report back to Averse.
 *              Help Averse uncover Beritra's plans and follow the Balaur's trail.
 */
public class _10505SneezeAttack extends AbstractQuestHandler {

	private final static int workItemId = 182215612; // Restored Beritra's Plans

	public _10505SneezeAttack() {
		super(10505);
	}

	@Override
	public void register() {
		// Balaur Maneuver Plans 702672
		// Averse 804707
		// Erinos 804708
		int[] npcs = { 702672, 804707, 804708 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		int[] items = { 182215609, 182215610, 182215611, 182215612 };
		for (int item : items) {
			qe.registerQuestItem(item, questId);
		}
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
			case 804707: // Averse
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Averse at Erivale Territory Village.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 1011);
						}

						if (dialogActionId == SETPRO1) {
							return defaultCloseDialog(env, var, var + 1);
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
			case 804708: // Erinos
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 1) { // Step 1: Talk with Investigator Erinos who has been dispatched to Twilight Temple.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 1352);
						}

						if (dialogActionId == SETPRO2) {
							return defaultCloseDialog(env, var, var + 1);
						}
					}

					if (var == 2) { // Step 2: Gather the Balaur Maneuver Plans and talk with Erinos. (Item check)
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 1693);
						}

						if (dialogActionId == CHECK_USER_HAS_QUEST_ITEM) {
							if (QuestService.collectItemCheck(env, true)) {
								qs.setQuestVar(var + 1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							}
							return sendQuestDialog(env, 10001);
						}
					}

					if (var == 3) { // Step 3: Talk with Erinos.
						if (dialogActionId == QUEST_SELECT) {
							return sendQuestDialog(env, 2034);
						}

						if (dialogActionId == SETPRO4) {
							return defaultCloseDialog(env, var, var + 1, workItemId, 1);
						}
					}
				}
				break;
			case 702672:
				if (dialogActionId == USE_OBJECT && var == 2) { // Step 2: Gather the Balaur Maneuver Plans and talk with Erinos.
					return useQuestObject(env, var, var, false, 0);
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
				if (var == 4) { // Step 4: Read the Deciphered Balaur Document and find out what it says.
					playQuestMovie(env, 992);
					qs.setQuestVar(var + 1);
					qs.setStatus(QuestStatus.REWARD);
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
