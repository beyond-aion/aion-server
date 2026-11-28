package quest.crafting;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.craft.CraftSkillUpdateService;
import com.aionemu.gameserver.utils.PacketSendUtility;

/**
 * @author Ritsu, Pad
 */
public class _19057MasterConstructorsPotential extends AbstractQuestHandler {

	private final static int[] recipesItemIds = { 152203543, 152203544 };
	private final static int[] recipesIds = { 155003543, 155003544 };

	public _19057MasterConstructorsPotential() {
		super(19057);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(798450).addOnQuestStart(questId);
		qe.registerQuestNpc(798450).addOnTalkEvent(questId);
		qe.registerQuestNpc(798451).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (dialogActionId == QUEST_SELECT && !CraftSkillUpdateService.getInstance().canLearnMoreMasterCraftingSkill(player)) {
			return sendQuestSelectionDialog(env);
		}

		if (qs == null || qs.isStartable()) {
			if (targetId == 798450) {
				if (dialogActionId == QUEST_SELECT)
					return sendQuestDialog(env, 4762);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 798451:
					switch (dialogActionId) {
						case QUEST_SELECT:
							switch (var) {
								case 0:
									return sendQuestDialog(env, 1011);
								case 2:
									return sendQuestDialog(env, 4080);
							}
							return false;
						case SETPRO10, SETPRO20:
							if (var != 0 && var != 2)
								return false;
							int fee = dialogActionId == SETPRO10 ? 167500 : 223000;
							int recipe = dialogActionId == SETPRO10 ? 152203543 : 152203544;
							if (!player.getInventory().tryDecreaseKinah(fee))
								return sendQuestDialog(env, 4400);
							if (!giveQuestItem(env, recipe, 1)) {
								player.getInventory().increaseKinah(fee);
								return true;
							}
							qs.setQuestVarById(0, 1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							return true;
					}
					return false;
				case 798450:
					switch (dialogActionId) {
						case QUEST_SELECT:
							return sendQuestDialog(env, 1352);
						case CHECK_USER_HAS_QUEST_ITEM:
							if (QuestService.collectItemCheck(env, true)) {
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 5);
							} else {
								int checkFailId = 3398;
								if (player.getRecipeList().isRecipePresent(recipesIds[0]) || player.getRecipeList().isRecipePresent(recipesIds[1]))
									checkFailId = 2716;
								else if (player.getInventory().getItemCountByItemId(recipesItemIds[0]) > 0
									|| player.getInventory().getItemCountByItemId(recipesItemIds[1]) > 0)
									checkFailId = 3057;

								if (checkFailId == 3398) {
									qs.setQuestVar(2);
									updateQuestStatus(env);
								}
								return sendQuestDialog(env, checkFailId);
							}
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 798450)
				return sendQuestEndDialog(env);
		}
		return false;
	}
}
