package quest.gelkmaros;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Nephis
 * @reworked & modified Gigi
 */
public class _20025QuestForSielsRelics extends QuestHandler {

	private final static int questId = 20025;
	private final static int[] npc_ids = { 799225, 799226, 799341, 798800, 204182, 799239, 204837, 799327, 799328 };

	public _20025QuestForSielsRelics() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		qe.registerQuestNpc(799342).addOnKillEvent(questId);
		qe.registerOnEnterZone(ZoneName.get("LF4_SENSORYAREA_Q10025_206106_1_600010000"), questId);
		qe.registerOnEnterZone(ZoneName.get("AETHERIC_FIELD_ROOM_300170000"), questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName == ZoneName.get("LF4_SENSORYAREA_Q10025_206106_1_600010000")) {
			final Player player = env.getPlayer();
			if (player == null) {
				return false;
			}
			final QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null || qs.getQuestVars().getQuestVars() != 12) {
				return false;
			}
			env.setQuestId(questId);
			qs.setQuestVar(13);
			updateQuestStatus(env);
			return true;
		} else if (zoneName == ZoneName.get("AETHERIC_FIELD_ROOM_300170000")) {
			final Player player = env.getPlayer();
			if (player == null) {
				return false;
			}
			final QuestState qs = player.getQuestStateList().getQuestState(questId);
			if (qs == null || qs.getQuestVars().getQuestVars() != 13) {
				return false;
			}
			WorldMapInstance instance = player.getPosition().getWorldMapInstance();
			if (instance != null) {
				Npc aethicFieldGenerator = instance.getNpc(700608);
				if (aethicFieldGenerator != null) {
					aethicFieldGenerator.getEffectController().removeEffect(19127);
				}
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (targetId == 799342) {
			if (qs.getQuestVarById(0) == 13) {
				qs.setStatus(QuestStatus.REWARD);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 20024, true);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 799225) {
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
			return false;
		} else if (qs.getStatus() != QuestStatus.START)
			return false;
		if (targetId == 799225) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1011);
				case SETPRO1:
					return defaultCloseDialog(env, 0, 1); // 1
			}
		} else if (targetId == 799226) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1352);
					else if (var == 4)
						return sendQuestDialog(env, 2375);
					else if (var == 9)
						return sendQuestDialog(env, 4080);
				case SETPRO2:
					return defaultCloseDialog(env, 1, 2); // 2
				case SETPRO5:
					return defaultCloseDialog(env, 4, 5); // 5
				case SETPRO10:
					return defaultCloseDialog(env, 9, 10); // 10
			}
		} else if (targetId == 799341) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 2)
						return sendQuestDialog(env, 1693);
					else if (var == 3)
						return sendQuestDialog(env, 2034);
				case SETPRO3:
					return defaultCloseDialog(env, 2, 3); // 3
				case SETPRO4:
					return defaultCloseDialog(env, 3, 4); // 4
			}
		} else if (targetId == 798800) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 5)
						return sendQuestDialog(env, 2716);
				case SETPRO6:
					return defaultCloseDialog(env, 5, 6); // 6
			}
		} else if (targetId == 204182) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 6)
						return sendQuestDialog(env, 3057);
				case SETPRO7:
					return defaultCloseDialog(env, 6, 7); // 7
			}
		} else if (targetId == 799239) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 7)
						return sendQuestDialog(env, 3398);
				case SETPRO8:
					return defaultCloseDialog(env, 7, 8); // 8
			}
		} else if (targetId == 204837) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 8)
						return sendQuestDialog(env, 3739);
				case CHECK_USER_HAS_QUEST_ITEM:
					if (QuestService.collectItemCheck(env, true)) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					} else
						return sendQuestDialog(env, 10001);

			}
		} else if (targetId == 799327) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 10)
						return sendQuestDialog(env, 1267);
				case SETPRO11:
					return defaultCloseDialog(env, 10, 11); // 11
			}
		} else if (targetId == 799328) {
			switch (env.getDialog()) {
				case QUEST_SELECT:
					if (var == 11)
						return sendQuestDialog(env, 1608);
				case SETPRO12:
					return defaultCloseDialog(env, 11, 12); // 12
			}
		}
		return false;
	}
}
