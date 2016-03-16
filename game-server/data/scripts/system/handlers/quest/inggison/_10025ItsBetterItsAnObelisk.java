package quest.inggison;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.PersistentState;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_ITEM_USAGE_ANIMATION;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Nephis
 * @reworked & modified Gigi
 */
public class _10025ItsBetterItsAnObelisk extends QuestHandler {

	private final static int questId = 10025;
	private final static int[] npc_ids = { 798927, 798932, 798933, 799023, 799024, 278500, 798926, 700607, 700637 };

	public _10025ItsBetterItsAnObelisk() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestItem(182206623, questId);
		qe.registerQuestItem(182206624, questId);
		qe.registerQuestItem(182206626, questId);
		qe.registerOnEnterZone(ZoneName.get("LF4_SENSORYAREA_Q10025_206106_1_600010000"), questId);
		qe.registerOnEnterZone(ZoneName.get("BESHMUNDIRS_WALK_1_300170000"), questId);
		for (int npc_id : npc_ids) {
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		}
		qe.registerOnLogOut(questId);
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() != QuestStatus.COMPLETE) {
			qs.setPersistentState(PersistentState.DELETED);
			return true;
		}
		return false;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("LF4_SENSORYAREA_Q10025_206106_1_600010000")) {
			final Player player = env.getPlayer();
			if (player == null) {
				return false;
			}
			// to prevent resetting to VAR 10 if leave BT and ran again into that Zone @Enomine
			final QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null || qs.getQuestVars().getQuestVars() > 9) {
				return false;
			}
			env.setQuestId(questId);
			qs.setQuestVar(10);
			updateQuestStatus(env);
			return true;
		} else if (zoneName == ZoneName.get("BESHMUNDIRS_WALK_1_300170000")) {
			final Player player = env.getPlayer();
			if (player == null) {
				return false;
			}
			final QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null || qs.getQuestVars().getQuestVars() != 12) {
				return false;
			}
			env.setQuestId(questId);
			qs.setQuestVar(10);
			updateQuestStatus(env);
			return true;
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		final Player player = env.getPlayer();
		final int id = item.getItemTemplate().getTemplateId();
		final int itemObjId = item.getObjectId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return HandlerResult.UNKNOWN;
		}

		final int var = qs.getQuestVarById(0);

		if (var == 4 && id == 182206623) {
			if (!player.isInsideZone(ZoneName.get("TEMPLE_OF_SCALES_210050000"))) {
				return HandlerResult.UNKNOWN;
			}
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					removeQuestItem(env, 182206623, 1);
					PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
				}
			}, 3000);
			return HandlerResult.SUCCESS;
		} else if (var == 6 && id == 182206624) {
			if (!player.isInsideZone(ZoneName.get("ALTAR_OF_AVARICE_210050000"))) {
				return HandlerResult.UNKNOWN;
			}
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					removeQuestItem(env, 182206624, 1);
					PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
					qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
					updateQuestStatus(env);
				}
			}, 3000);
			return HandlerResult.SUCCESS;

		} else if (var == 12 && id == 182206626) {
			if (!player.isInsideZone(ZoneName.get("ANGRIEF_BULWARK_210050000"))) {
				return HandlerResult.UNKNOWN;
			}
			PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 3000, 0, 0), true);
			ThreadPoolManager.getInstance().schedule(new Runnable() {

				@Override
				public void run() {
					removeQuestItem(env, 182206626, 1);
					PacketSendUtility.broadcastPacket(player, new SM_ITEM_USAGE_ANIMATION(player.getObjectId(), itemObjId, id, 0, 1, 0), true);
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
				}
			}, 3000);
			return HandlerResult.SUCCESS;

		}
		return HandlerResult.UNKNOWN;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) {
			return false;
		}
		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc) {
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		}

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798926) {
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id()) {
					return sendQuestDialog(env, 5);
				} else {
					return sendQuestEndDialog(env);
				}
			}
			return false;
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 798927) {// Versetti
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 0) {
						return sendQuestDialog(env, 1011);
					}
				case SETPRO1:
					if (var == 0) {
						return defaultCloseDialog(env, 0, 1); // 1
					}
			}
		} else if (targetId == 798932) {// Secundila
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 1) {
						return sendQuestDialog(env, 1352);
					}
				case SETPRO2:
					if (var == 1) {
						return defaultCloseDialog(env, 1, 2); // 2
					}
			}
		} else if (targetId == 798933) {// Phailos
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 2) {
						return sendQuestDialog(env, 1693);
					}
				case SETPRO3:
					if (var == 2) {
						return defaultCloseDialog(env, 2, 3); // 3
					}
			}
		} else if (targetId == 799023) {// Thalia
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 3) {
						return sendQuestDialog(env, 2034);
					}
				case SETPRO4:
					if (var == 3) {
						return defaultCloseDialog(env, 3, 4, 182206623, 1, 0, 0); // 4
					}
			}
		} else if (targetId == 799024) {// Kerus
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 5) {
						return sendQuestDialog(env, 2716);
					} else if (var == 7) {
						return sendQuestDialog(env, 3398);
					}
				case SETPRO6:
					if (var == 5) {
						return defaultCloseDialog(env, 5, 6, 182206624, 1, 0, 0); // 6
					}
				case SETPRO8:
					if (var == 7) {
						return defaultCloseDialog(env, 7, 8); // 8
					}
			}
		} else if (targetId == 278500) {// Yudito
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 8) {
						return sendQuestDialog(env, 3739);
					}
				case SETPRO9:
					if (var == 8) {
						return defaultCloseDialog(env, 8, 9); // 9
					}
			}
		} else if (targetId == 700607) {
			switch (env.getDialog()) {
				case USE_OBJECT:
					if (var == 10) {
						qs.setQuestVar(11);
						updateQuestStatus(env);
					}
			}
		} else if (targetId == 700637) {
			switch (env.getDialog()) {
				case USE_OBJECT:
					if (var == 11) {
						qs.setQuestVar(12);
						updateQuestStatus(env);
						WorldMapInstance instance = player.getPosition().getWorldMapInstance();
						if (instance != null) {
							Npc aethicFieldGenerator = instance.getNpc(700608);
							if (aethicFieldGenerator != null) {
								aethicFieldGenerator.getEffectController().removeEffect(19127);
							}
						}
					}
			}
		}
		return false;
	}
}
