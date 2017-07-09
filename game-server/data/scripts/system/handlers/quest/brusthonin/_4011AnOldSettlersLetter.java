package quest.brusthonin;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Leunam
 */
public class _4011AnOldSettlersLetter extends AbstractQuestHandler {

	private final static int[] npc_ids = { 730139, 205132, 203522 };

	public _4011AnOldSettlersLetter() {
		super(4011);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(730139).addOnQuestStart(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 730139) {
			if (env.getDialogActionId() == USE_OBJECT)
				return sendQuestDialog(env, 1011);
			else if (env.getDialogActionId() == SETPRO1) {
				QuestService.startQuest(env);
				PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 0));
				return true;
			}
		}
		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);
		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205132) {
				if (env.getDialogActionId() == USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() != QuestStatus.START) {
			return false;
		}
		if (targetId == 205132) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 0)
						return sendQuestDialog(env, 1352);
					return false;
				case SETPRO2:
					if (var == 0) {
						qs.setQuestVarById(0, var + 1);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
					return false;
			}
		} else if (targetId == 203522) {
			switch (env.getDialogActionId()) {
				case QUEST_SELECT:
					if (var == 1)
						return sendQuestDialog(env, 1693);
					return false;
				case SET_SUCCEED:
					if (var == 1) {
						qs.setStatus(QuestStatus.REWARD);
						updateQuestStatus(env);
						PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
						return true;
					}
					return false;
			}
		}
		return false;
	}
}
