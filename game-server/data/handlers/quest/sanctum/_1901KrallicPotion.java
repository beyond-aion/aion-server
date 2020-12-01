package quest.sanctum;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.items.storage.Storage;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author edynamic90
 */
public class _1901KrallicPotion extends AbstractQuestHandler {

	public _1901KrallicPotion() {
		super(1901);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203830).addOnQuestStart(questId);
		qe.registerQuestNpc(203830).addOnTalkEvent(questId);
		qe.registerQuestNpc(798026).addOnTalkEvent(questId);
		qe.registerQuestNpc(798025).addOnTalkEvent(questId);
		qe.registerQuestNpc(203131).addOnTalkEvent(questId);
		qe.registerQuestNpc(798003).addOnTalkEvent(questId);
		qe.registerQuestNpc(203864).addOnTalkEvent(questId);
		/* qe.setQuestItemIds(182204115).add(questId); */
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (targetId == 203830)// Marmeia
		{
			if (env.getDialogActionId() == QUEST_SELECT)
				return sendQuestDialog(env, 1011);
			else
				return sendQuestStartDialog(env);
		} else {
			if (targetId == 203864) {
				if (env.getDialogActionId() == QUEST_SELECT)
					return sendQuestDialog(env, 2375);
				else if (env.getDialogActionId() == SELECT_QUEST_REWARD && qs.getStatus() != QuestStatus.COMPLETE) {
					return sendQuestEndDialog(env);
				} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
					if (env.getDialogActionId() == USE_OBJECT)
						return sendQuestDialog(env, 3398);
					return sendQuestEndDialog(env);
				}
			} else if (qs != null) {
				if (qs.getStatus() == QuestStatus.START) {
					int var = qs.getQuestVarById(0);
					switch (targetId) {
						case 798026:// Kunberunerk
							switch (env.getDialogActionId()) {
								case QUEST_SELECT:
									if (var == 0)
										return sendQuestDialog(env, 1352);
									else if (var == 5)
										return sendQuestDialog(env, 3057);
									return false;
								case SELECT2_2:
									Storage inventory = player.getInventory();
									if (inventory.tryDecreaseKinah(10000)) {
										return sendQuestDialog(env, 1438);
									} else
										return sendQuestDialog(env, 1523);
								case SETPRO1:
									qs.setQuestVarById(0, 5); // Reward (5)
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(env);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return true;
								case SETPRO2:
									qs.setQuestVarById(0, 1); // 1
									updateQuestStatus(env);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return true;
								case SETPRO7:
									qs.setQuestVarById(0, 5); // 5
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(env);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return true;
								default:
									return sendQuestStartDialog(env);
							}
						case 798025:// Mapireck
							switch (env.getDialogActionId()) {
								case QUEST_SELECT:
									if (var == 1)
										return sendQuestDialog(env, 1693);
									else if (var == 4)
										return sendQuestDialog(env, 2716);
									return false;
								case SETPRO3:
									qs.setQuestVarById(0, var + 1);// var==2
									updateQuestStatus(env);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return true;
								case SETPRO6:
									removeQuestItem(env, 182206000, 1);
									qs.setQuestVarById(0, var + 1);// var==5
									updateQuestStatus(env);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return true;
							}
							return false;
						case 203131:// Maniparas
							switch (env.getDialogActionId()) {
								case QUEST_SELECT:
									return sendQuestDialog(env, 2034);
								case SETPRO4:
									qs.setQuestVarById(0, var + 1);// var==3
									updateQuestStatus(env);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return true;
							}
							return false;
						case 798003:// Gaphyrk
							switch (env.getDialogActionId()) {
								case QUEST_SELECT:
									return sendQuestDialog(env, 2375);
								case SETPRO5:
									if (player.getInventory().getItemCountByItemId(182206000) == 0)
										if (!giveQuestItem(env, 182206000, 1))
											return true;
									qs.setQuestVarById(0, var + 1);// var==4
									updateQuestStatus(env);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return true;
							}
					}
				}
			}
			return false;
		}
	}
}
