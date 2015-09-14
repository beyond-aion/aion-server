package quest.carving_fortune;

import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Mr. Poke, dune11
 */
public class _2097SpiritBlade extends QuestHandler {

	private final static int questId = 2097;
	private final static int[] npc_ids = { 203550, 700509, 700510, 203546, 279034 };

	public _2097SpiritBlade() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnLevelUp(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		qe.registerQuestItem(182207085, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;

		int var = qs.getQuestVars().getQuestVars();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203550: // MUNIN
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 0)
								return sendQuestDialog(env, 1011);
						case SETPRO1:
							if (var == 0) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
					break;
				case 203546: // SKULID
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 1)
								return sendQuestDialog(env, 1352);
							return true;
						case SETPRO2:
							if (var == 1) {
								qs.setQuestVarById(0, var + 1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
							}
					}
					break;
				case 279034:
					switch (env.getDialog()) {
						case QUEST_SELECT:
							if (var == 2)
								return sendQuestDialog(env, 1693);
						case CHECK_USER_HAS_QUEST_ITEM:
							if (var == 2) {
								if (QuestService.collectItemCheck(env, true)) {
									if (!giveQuestItem(env, 182207085, 1))
										return true;
									qs.setStatus(QuestStatus.REWARD);
									updateQuestStatus(env);
									PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
									return sendQuestDialog(env, 10000);
								} else
									return sendQuestDialog(env, 10001);
							}
					}
					break;
				case 700509:
				case 700510:
					return true; // looting
			}
		} else if (qs.getStatus() == QuestStatus.REWARD && targetId == 203550)
			return sendQuestEndDialog(env);
		return false;
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env);
	}

}
