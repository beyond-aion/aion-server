package quest.beluslan;

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
public class _2538CurseoftheWereRibbit extends AbstractQuestHandler {

	public _2538CurseoftheWereRibbit() {
		super(2538);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204827).addOnQuestStart(questId);
		qe.registerQuestNpc(204827).addOnTalkEvent(questId);
		qe.registerQuestNpc(790002).addOnTalkEvent(questId);
		qe.registerQuestNpc(204805).addOnTalkEvent(questId);
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
			if (targetId == 204827) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 204827) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 3) {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							return sendQuestDialog(env, 2375);
						}
						return false;
					case SELECT_QUEST_REWARD:
						return sendQuestDialog(env, 5);
				}
			}
			if (targetId == 790002) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1352);
						if (var == 2)
							return sendQuestDialog(env, 2034);
						return false;
					case SETPRO1:
						if (var == 0) {
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
						return false;
					case SETPRO3:
						if (var == 2) {
							removeQuestItem(env, 182204517, 1);
							if (!giveQuestItem(env, 182204518, 1))
								return true;
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
				}
			}
			if (targetId == 204805) {
				switch (dialogActionId) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1693);
						return false;
					case SETPRO2:
						if (var == 1) {
							if (!giveQuestItem(env, 182204517, 1))
								return true;
							qs.setQuestVarById(0, qs.getQuestVarById(0) + 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
						}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204827) {
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
