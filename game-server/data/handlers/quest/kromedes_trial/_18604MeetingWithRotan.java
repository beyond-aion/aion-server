package quest.kromedes_trial;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.Race;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestActionType;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author Rolandas, Pad, Neon
 */
public class _18604MeetingWithRotan extends AbstractQuestHandler {

	public _18604MeetingWithRotan() {
		super(18604);
	}

	@Override
	public void register() {
		qe.registerOnEnterZone(ZoneName.get("GRAND_CAVERN_300230000"), questId);
		qe.registerQuestNpc(700961).addOnTalkEvent(questId); // Grave Robber's Corpse
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();

		if (env.getTargetId() != 700961)
			return false;
		if (player.getRace() != Race.ELYOS) // both factions use the same npc for rotan quest (see quest 28604)
			return false;

		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null) { // in case quest wasn't started by zone enter or player aborted
			if (env.getDialogActionId() == USE_OBJECT) {
				return sendQuestDialog(env, DialogPage.ASK_QUEST_ACCEPT_WINDOW.id());
			} else if (env.getDialogActionId() == QUEST_ACCEPT_1) {
				if (QuestService.startQuest(env))
					return sendQuestDialog(env, 10002);
			} else {
				return closeDialogWindow(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (env.getDialogActionId() == USE_OBJECT) {
				return sendQuestDialog(env, 10002);
			} else {
				changeQuestStep(env, 0, 0, true);
				return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		} else if (qs.getStatus() == QuestStatus.COMPLETE) { // handling when quest is already done
			env.setQuestId(0);
			if (checkItemExistence(env, 164000141, 1, false)) // player already has rotan summon device
				return sendQuestDialog(env, DialogPage.NO_RIGHT.id());
			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.STR_MSG_IDCROMEDE_SKILL_01());
			giveQuestItem(env, 164000141, 1);
			return sendQuestDialog(env, 1012);
		}

		return false;
	}

	@Override
	public boolean onCanAct(QuestEnv env, QuestActionType questEventType, Object... objects) {
		// allow to use body even when quest is completed
		return env.getTargetId() == 700961 && questEventType == QuestActionType.ACTION_ITEM_USE;
	}

	@Override
	public boolean onEnterZoneEvent(QuestEnv env, ZoneName zoneName) {
		if (zoneName != ZoneName.get("GRAND_CAVERN_300230000"))
			return false;

		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);

		if (qs == null || qs.isStartable()) {
			env.setQuestId(questId);
			return QuestService.startQuest(env);
		}

		return false;
	}

}
