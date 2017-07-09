package quest.altgard;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu
 */
public class _2279SolidProof extends AbstractQuestHandler {

	public _2279SolidProof() {
		super(2279);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203557).addOnQuestStart(questId); // Suthran
		qe.registerQuestNpc(203557).addOnTalkEvent(questId);
		qe.registerQuestNpc(203590).addOnTalkEvent(questId); // Emgata
		qe.registerQuestNpc(203682).addOnTalkEvent(questId); // Zemurru's Spirit
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (targetId == 203557) {// Suthran
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 203590) {// Emgata
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
						return false;
					case SETPRO1:
						if (var == 0) {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
				}
			}
			if (targetId == 203682) {// Zemurru's Spirit
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1693);
						return false;
					case SETPRO2:
						if (var == 1) {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
				}
			}
			if (targetId == 203557) {// Suthran
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 2) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SELECT_QUEST_REWARD:
						return sendQuestEndDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203557) {// Suthran
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
