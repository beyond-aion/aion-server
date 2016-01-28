package quest.cygnea;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @Author Majka
 * @Description
 * Talk with Eukraton at Aequis Advance Post.
 * Eliminate Aetheric Field Guards (3) at Dragon Lord's Gardens.
 * Obtain the Old Balaur Document from the Scattered Balaur Document and take it to Euvia.
 * Talk with Euvia at Dragon Lord's Gardens.
 * Read the Deciphered Balaur Document and find out what it says.
 * Talk with Euvia at Dragon Lord's Gardens.
 * Examine the Aetheric Field Stone of Earth.
 * Report back to Eukraton.
 *
 * Meet Eukraton, eliminate Aetheric Field Guards, and discover the secrets of Tiamat's ruins.
 */
public class _10503GuardDownSecretsOut extends QuestHandler {

	private final static int questId = 10503;
	private final static int mobTargetId = 236252; // Aetheric Field Guard
	private final static int collectItemId = 182215603; // Old Balaur Document
	private final static int workItemId = 182215604; // Deciphered Balaur Document

	public _10503GuardDownSecretsOut() {
		super(questId);
	}

	@Override
	public void register() {
		// Eukraton 804704
		// Euvia 804705
		// Scattered Balaur Document 702670
		int[] npcs = { 702670, 804704, 804705 };
		for (int npc : npcs) {
			qe.registerQuestNpc(npc).addOnTalkEvent(questId);
		}
		qe.registerQuestNpc(mobTargetId).addOnKillEvent(questId);
		qe.registerQuestItem(collectItemId, questId);
		qe.registerQuestItem(workItemId, questId);
		qe.registerOnEnterZone(ZoneName.get("LF5_SENSORYAREA_Q10503_206364_3_210070000"), questId); // Aetheric Field Stone of Earth zone
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();
		DialogAction dialog = env.getDialog();

		switch (targetId) {
			case 804704: // Eukraton
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 0) { // Step 0: Talk with Eukraton at Aequis Advance Post.
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 1011);
						}

						if (dialog == DialogAction.SETPRO1) {
							return defaultCloseDialog(env, var, var + 1);
						}
					}
				}

				if (qs.getStatus() == QuestStatus.REWARD) {
					if (dialog == DialogAction.USE_OBJECT) {
						return sendQuestDialog(env, 10002);
					}
					return sendQuestEndDialog(env);
				}
				break;
			case 804705: // Euvia
				if (qs.getStatus() == QuestStatus.START) {
					if (var == 2) { // Step 2: Obtain the Old Balaur Document from the Scattered Balaur Document and take it to Euvia
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 1693);
						}

						if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM) {
							long itemCount = player.getInventory().getItemCountByItemId(collectItemId);
							if (itemCount > 0) {
								removeQuestItem(env, collectItemId, itemCount);
								qs.setQuestVar(var + 1);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
							} else {
								return sendQuestDialog(env, 10001);
							}
						}
					}

					if (var == 3) { // Step 3: Talk with Euvia at Dragon Lord's Gardens.
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2034);
						}

						if (dialog == DialogAction.SETPRO4) {
							giveQuestItem(env, workItemId, 1);
							return defaultCloseDialog(env, var, var + 1);
						}
					}

					if (var == 5) { // Step 5: Talk with Euvia at Dragon Lord's Gardens.
						if (dialog == DialogAction.QUEST_SELECT) {
							return sendQuestDialog(env, 2716);
						}

						if (dialog == DialogAction.SETPRO6) {
							return defaultCloseDialog(env, var, var + 1);
						}
					}
				}
				break;
			case 702670:
				if (dialog == DialogAction.USE_OBJECT && var == 2) { // Step 2: Obtain the Old Balaur Document from the Scattered Balaur Document and take it to Euvia.
					return useQuestObject(env, 2, 2, false, 0);
				}
				break;
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) { // Step 6: Examine the Aetheric Field Stone of Earth.

		if (zoneName == ZoneName.get("LF5_SENSORYAREA_Q10503_206364_3_210070000")) {

			Player player = env.getPlayer();
			if (player == null) {
				return false;
			}

			QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs != null && qs.getStatus() == QuestStatus.START) {
				int var = qs.getQuestVarById(0);

				if (var == 6) {
					qs.setQuestVar(var+1);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					playQuestMovie(env, 991);
					player.getMoveController().abortMove();
				}
			}
			return true;
		}
		return false;
	}
	
	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = env.getTargetId();

		if (var == 1) { // Step 1: Eliminate Aetheric Field Guards (3) at Dragon Lord's Gardens.
			if (defaultOnKillEvent(env, targetId, 0, 3, 1)) { // 1-2 with varNum 1
				int varKill = qs.getQuestVarById(1); // Kill counter
				if (varKill >= 3) {
					qs.setQuestVar(2);
					updateQuestStatus(env);
				}
				return true;
			}
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
					qs.setQuestVar(var + 1);
					updateQuestStatus(env);
				}
			}
		}, 1000);
		return HandlerResult.SUCCESS;
	}
	
	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 10500);
	}
}
