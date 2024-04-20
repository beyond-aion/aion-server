package quest.crafting;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.craft.CraftSkillUpdateService;

/**
 * @author Thuatan, vlog, Pad
 */
public class _29038MasterCooksPotential extends AbstractQuestHandler {

	public _29038MasterCooksPotential() {
		super(29038);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(204100).addOnQuestStart(questId);
		qe.registerQuestNpc(204100).addOnTalkEvent(questId);
		qe.registerQuestNpc(204101).addOnTalkEvent(questId);
		qe.registerOnFailCraft(182207907, questId);
		qe.registerOnFailCraft(182207908, questId);
		qe.registerOnFailCraft(182207909, questId);
		qe.registerOnFailCraft(182207910, questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (dialogActionId == QUEST_SELECT && !CraftSkillUpdateService.getInstance().canLearnMoreMasterCraftingSkill(player)) {
			return sendQuestSelectionDialog(env);
		}

		if (qs == null || qs.isStartable()) {
			if (targetId == 204100) { // Lainita
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			switch (targetId) {
				case 204101: // Daraia
					long kinah = player.getInventory().getKinah();
					switch (dialogActionId) {
						case QUEST_SELECT:
							if (kinah >= 6500) {
								switch (var) {
									case 0: {
										return sendQuestDialog(env, 1011);
									}
									case 3: {
										return sendQuestDialog(env, 1352);
									}
									case 6: {
										return sendQuestDialog(env, 1693);
									}
									case 9: {
										return sendQuestDialog(env, 2034);
									}
									case 1: {
										if (!player.getRecipeList().isRecipePresent(155007241))
											return sendQuestDialog(env, 4081);
										return false;
									}
									case 4: {
										if (!player.getRecipeList().isRecipePresent(155007242))
											return sendQuestDialog(env, 4166);
										return false;
									}
									case 7: {
										if (!player.getRecipeList().isRecipePresent(155007243))
											return sendQuestDialog(env, 4251);
										return false;
									}
									case 10: {
										if (!player.getRecipeList().isRecipePresent(155007244))
											return sendQuestDialog(env, 4336);
										return false;
									}
								}
								return false;
							} else {
								return sendQuestDialog(env, 4400);
							}
						case SETPRO10:
							player.getInventory().decreaseKinah(6500);
							if (var == 0) {
								return defaultCloseDialog(env, 0, 2, 152207202, 1, 0, 0); // 2
							} else if (var == 1) {
								return defaultCloseDialog(env, 1, 2, 152207202, 1, 0, 0); // 2
							}
							return false;
						case SETPRO20:
							player.getInventory().decreaseKinah(6500);
							if (var == 3) {
								return defaultCloseDialog(env, 3, 5, 152207203, 1, 0, 0); // 5
							} else if (var == 4) {
								return defaultCloseDialog(env, 4, 5, 152207203, 1, 0, 0); // 5
							}
							return false;
						case SETPRO30:
							player.getInventory().decreaseKinah(6500);
							if (var == 6) {
								return defaultCloseDialog(env, 6, 8, 152207204, 1, 0, 0); // 8
							} else if (var == 7) {
								return defaultCloseDialog(env, 7, 8, 152207204, 1, 0, 0); // 8
							}
							return false;
						case SETPRO40:
							player.getInventory().decreaseKinah(6500);
							if (var == 9) {
								return defaultCloseDialog(env, 9, 11, 152207205, 1, 0, 0); // 11
							} else if (var == 10) {
								return defaultCloseDialog(env, 10, 11, 152207205, 1, 0, 0); // 11
							}
							return false;
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
					break;
				case 204100: // Lainita
					switch (dialogActionId) {
						case QUEST_SELECT:
							switch (var) {
								case 2: {
									return sendQuestDialog(env, 1097);
								}
								case 5: {
									return sendQuestDialog(env, 1438);
								}
								case 8: {
									return sendQuestDialog(env, 1779);
								}
								case 11: {
									return sendQuestDialog(env, 2120);
								}
							}
							return false;
						case SETPRO11:
							return checkItemExistence(env, 2, 3, false, 182207907, 1, true, 1182, 2716, 0, 0); // 3
						case SETPRO21:
							return checkItemExistence(env, 5, 6, false, 182207908, 1, true, 1523, 3057, 0, 0); // 6
						case SETPRO31:
							return checkItemExistence(env, 8, 9, false, 182207909, 1, true, 1864, 3398, 0, 0); // 9
						case SETPRO41:
							return checkItemExistence(env, 11, 11, true, 182207910, 1, true, 5, 3057, 0, 0); // reward
						case FINISH_DIALOG:
							return sendQuestSelectionDialog(env);
					}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204100) { // Lainita
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}

	@Override
	public boolean onFailCraftEvent(QuestEnv env, int itemId) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			switch (itemId) {
				case 182207907:
					changeQuestStep(env, 2, 1); // 1
					return true;
				case 182207908:
					changeQuestStep(env, 5, 4); // 4
					return true;
				case 182207909:
					changeQuestStep(env, 8, 7); // 7
					return true;
				case 182207910:
					changeQuestStep(env, 11, 10); // 10
					return true;
			}
		}
		return false;
	}
}
