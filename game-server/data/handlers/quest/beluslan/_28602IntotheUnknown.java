package quest.beluslan;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldMapType;

/**
 * @author Gigi, Majka
 */
public class _28602IntotheUnknown extends AbstractQuestHandler {

	public _28602IntotheUnknown() {
		super(28602);
	}

	@Override
	public void register() {
		// Bridget 205234
		// Maga's Potions 730308
		// Robstin's Corpse 700939
		int[] npc_ids = { 205234, 730308, 700939 };
		qe.registerOnEnterWorld(questId);
		qe.registerOnDie(questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		qe.registerQuestNpc(205234).addOnQuestStart(questId);
		qe.registerQuestNpc(217005).addOnKillEvent(questId); // Shadow Judge Kaliga
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() != 300230000) {
				int var = qs.getQuestVarById(0);
				if (var > 0) {
					changeQuestStep(env, var, 0);
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onDieEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var > 0) {
				changeQuestStep(env, var, 0);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			return defaultOnKillEvent(env, 217005, 3, true); // Shadow Judge Kaliga
		}
		return false;
	}

	@Override
	public void onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId == 454)
			changeQuestStep(env, 1, 2);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		int targetId = env.getTargetId();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.isStartable()) {
			if (targetId == 205234) { // Bridget
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				}
				return sendQuestStartDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == 205234) {
				if (env.getDialogActionId() == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (env.getDialogActionId() == SETPRO1) {
					WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(WorldMapType.KROMEDES_TRIAL.getId(), player);
					TeleportService.teleportTo(player, newInstance, 244.98566f, 244.14162f, 189.52058f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
					changeQuestStep(env, 0, 1); // 1
					return closeDialogWindow(env);
				}
			} else if (targetId == 730308) {
				if (env.getDialogActionId() == USE_OBJECT) {
					if (var == 1) {
						return sendQuestDialog(env, 1352);
					}
				} else if (env.getDialogActionId() == SELECT2_1) {
					return sendQuestDialog(env, 1353);
				} else if (env.getDialogActionId() == SETPRO2) {

					// Check item
					if (var == 1) {
						if (checkItemExistence(env, 185000109, 1, true)) { // Hisen's key
							changeQuestStep(env, 1, 2); // 2
							TeleportService.teleportTo(player, 300230000, 687.56116f, 681.68225f, 200.28648f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
							return closeDialogWindow(env);
						} else {
							return sendQuestDialog(env, 10001);
						}
					} else {
						TeleportService.teleportTo(player, 300230000, 687.56116f, 681.68225f, 200.28648f, (byte) 30, TeleportAnimation.FADE_OUT_BEAM);
						return closeDialogWindow(env);
					}
				}
			} else if (targetId == 700939) { // Robstin's Corpse
				if (env.getDialogActionId() == USE_OBJECT) {
					if (var == 2) {
						return sendQuestDialog(env, 1693);
					}
				} else if (env.getDialogActionId() == SETPRO3) {
					if (!player.getEffectController().hasAbnormalEffect(19288)) {
						SkillEngine.getInstance().applyEffectDirectly(19288, player, player); // Rage of Kromede
					}
					return defaultCloseDialog(env, 2, 3);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205234) { // Bridget
				if (env.getDialogActionId() == USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				}
				return sendQuestEndDialog(env);
			}
		}
		return false;
	}
}
