package quest.daevanion;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.DialogPage;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;

/**
 * @author Tiger, Wakizashi, Rolandas, Pad, Neon
 */
public class _1993AnotherBeginning extends AbstractQuestHandler {

	private final static int dialogs[] = { 1013, 1034, 1055, 1076, 5103, 1098, 1119, 1140, 1161, 5104, 1183, 1204, 1225, 1246, 5105, 1268, 1289, 1310,
		1331, 5106, 2376, 2461, 2546, 2631, 2632 };
	private final static int items[][] = { { 110600834, 110600835 }, { 113600800, 113600801 }, { 114600794, 114600795 }, { 112600785, 112600786 },
		{ 111600813, 111600814 }, // Plate
		{ 110300881, 110300882 }, { 113300860, 113300861 }, { 114300893, 114300894 }, { 112300784, 112300785 }, { 111300834, 111300835 }, // Leather
		{ 110100931, 110100932 }, { 113100843, 113100844 }, { 114100866, 114100867 }, { 112100790, 112100791 }, { 111100831, 111100832 }, // Cloth
		{ 110500849, 110500850 }, { 113500827, 113500828 }, { 114500837, 114500838 }, { 112500774, 112500775 }, { 111500821, 111500822 }, // Chain
		{ 110301532, 110301543 }, { 113301497, 113301509 }, { 114301526, 114301538 }, { 112301412, 112301421 }, { 111301470, 111301480 } };// Magical
																																																																				// Leather

	public _1993AnotherBeginning() {
		super(1993);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(203753).addOnQuestStart(questId);
		qe.registerQuestNpc(203753).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		if (env.getTargetId() != 203753) // Sibylla
			return false;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();

		if (qs == null || qs.isStartable()) {
			if (dialogActionId == EXCHANGE_COIN) {
				QuestService.startQuest(env);
				return sendQuestDialog(env, 1011);
			} else {
				return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (dialogActionId == EXCHANGE_COIN)
				return sendQuestDialog(env, 1011);

			for (int dialogIndex = 0; dialogIndex < dialogs.length; dialogIndex++) {
				if (dialogs[dialogIndex] == dialogActionId) {
					for (int itemId : items[dialogIndex]) {
						if (player.getInventory().getItemCountByItemId(itemId) > 0) {
							qs.setRewardGroup(dialogIndex);
							return sendQuestDialog(env, 1013);
						}
					}
					return sendQuestDialog(env, 1352);
				}
			}

			switch (dialogActionId) {
				case SETPRO1: // get plate
				case SETPRO2: // get leather
				case SETPRO3: // get cloth
				case SETPRO4: // get chain
				case SETPRO5: // get magic leather
					if (player.getInventory().getItemCountByItemId(186000041) == 0) // Daevanion's Light
						return sendQuestDialog(env, 1009);
					Integer savedData = qs.getRewardGroup();
					int itemIdToRemove = 0;
					for (int itemId : items[savedData]) {
						if (player.getInventory().getItemCountByItemId(itemId) > 0)
							itemIdToRemove = itemId;
					}
					if (itemIdToRemove == 0)
						return sendQuestDialog(env, 1352);
					changeQuestStep(env, 0, 0, true);
					removeQuestItem(env, 186000041, 1);
					removeQuestItem(env, itemIdToRemove, 1);
					qs.setRewardGroup(dialogActionId - SETPRO1); // 0 - 4
					return sendQuestDialog(env, DialogPage.getRewardPageByIndex(qs.getRewardGroup()).id());
			}
			return super.onDialogEvent(env);
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			return sendQuestEndDialog(env);
		}
		return false;
	}
}
