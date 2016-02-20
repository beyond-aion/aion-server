package quest.crafting;

import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.craft.CraftSkillUpdateService;

/**
 * @author Gigi
 * @modified Pad
 */
public class _2974ExpertArmorsmithsTest extends QuestHandler {

	private final static int questId = 2974;

	public _2974ExpertArmorsmithsTest() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204106).addOnQuestStart(questId);
		qe.registerQuestNpc(204106).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (dialog == DialogAction.QUEST_SELECT && !CraftSkillUpdateService.getInstance().canLearnMoreExpertCraftingSkill(player)) {
			return sendQuestSelectionDialog(env);
		}

		if (qs == null || qs.getStatus() == QuestStatus.NONE) {
			if (targetId == 204106) {
				if (dialog == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else
					return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 204106: {
					switch (dialog) {
						case QUEST_SELECT: {
							long itemCount1 = player.getInventory().getItemCountByItemId(182207965);
							if (itemCount1 > 0) {
								removeQuestItem(env, 182207965, 1);
								qs.setStatus(QuestStatus.REWARD);
								updateQuestStatus(env);
								return sendQuestDialog(env, 2375);
							} else
								return sendQuestDialog(env, 2716);
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204106) {
				if (dialog == DialogAction.CHECK_USER_HAS_QUEST_ITEM)
					return sendQuestDialog(env, 5);
				else
					return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
