package quest.greater_stigma;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

/**
 * @author JIEgOKOJI
 * @modified kecimis
 */
public class _3930SecretoftheShatteredStigma extends QuestHandler {

	private final static int questId = 3930;

	public _3930SecretoftheShatteredStigma() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203711).addOnQuestStart(questId);// miriya start
		qe.registerQuestNpc(203833).addOnTalkEvent(questId);// Xenophon
		qe.registerQuestNpc(798321).addOnTalkEvent(questId);// Koruchinerk
		qe.registerQuestNpc(700562).addOnTalkEvent(questId);// Strongbox
		qe.registerQuestNpc(203711).addOnTalkEvent(questId);// Miriya
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		// Instanceof
		final Player player = env.getPlayer();
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);

		// NPC Quest :
		// 0 - Vergelmir start
		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 203711)// Miriya
			{
				// Get QUEST_SELECT in the eddit-HyperLinks.xml
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					// Send HTML_PAGE_SELECT_NONE to eddit-HtmlPages.xml
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);

			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {

			switch (targetId) {

			// Xenophon
				case 203833:
					if (var == 0) {
						switch (env.getDialog()) {
						// Get QUEST_SELECT in the eddit-HyperLinks.xml
							case QUEST_SELECT:
								// Send select1 to eddit-HtmlPages.xml
								return sendQuestDialog(env, 1011);
								// Get SETPRO1 in the eddit-HyperLinks.xml
							case SETPRO1:
								qs.setQuestVar(1);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
						}
					}
					// 2 / 4- Talk with Koruchinerk
				case 798321:
					if (var == 1) {
						switch (env.getDialog()) {
						// Get QUEST_SELECT in the eddit-HyperLinks.xml
							case QUEST_SELECT:
								// Send select1 to eddit-HtmlPages.xml
								return sendQuestDialog(env, 1352);
								// Get SETPRO1 in the eddit-HyperLinks.xml
							case SETPRO2:
								qs.setQuestVar(2);
								updateQuestStatus(env);
								PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
								return true;
						}
					} else if (var == 2) {
						switch (env.getDialog()) {
						// Get QUEST_SELECT in the eddit-HyperLinks.xml
							case QUEST_SELECT:
								// Send select1 to eddit-HtmlPages.xml
								return sendQuestDialog(env, 1693);
								// Get SETPRO1 in the eddit-HyperLinks.xml
							case CHECK_USER_HAS_QUEST_ITEM:
								if (player.getInventory().getItemCountByItemId(182206075) < 1) {
									// player doesn't own required item
									return sendQuestDialog(env, 10001);
								}
								removeQuestItem(env, 182206075, 1);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 10000);
						}

					}
					return false;
				case 700562:
					if (var == 2) {
						ThreadPoolManager.getInstance().schedule(new Runnable() {

							@Override
							public void run() {
								updateQuestStatus(env);
							}
						}, 3000);
						return true;
					}
					break;
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203711)// Miriya
			{
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 10002);
				else if (env.getDialogId() == DialogAction.SELECT_QUEST_REWARD.id())
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
