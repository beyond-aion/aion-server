package quest.beluslan;

import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.SystemMessageId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * Meet with Tristran (204774). Talk with Stua (204809). Infiltrate the Port through the Secret Port Entrance (700359). Don't blow your cover! Find
 * the Alquimia Entrance, break through to the Daevic Genesis Lab, and destroy the Research Center Power Generator (700349) (1). Escape from the
 * research center and send a Signal Flare signal (182204317) to begin the attack. Report to Tristran.
 * 
 * @author Hellboy aion4Free MetaWind
 * @reworked vlog
 * @modified Enomine
 */
public class _2058ASpyAmongtheLepharists extends QuestHandler {

	private final static int questId = 2058;
	private final static int[] npc_ids = { 204774, 204809, 700359 };

	public _2058ASpyAmongtheLepharists() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnDie(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerOnEnterWorld(questId);
		qe.registerOnEnterZoneMissionEnd(questId);
		qe.registerOnLevelUp(questId);
		qe.registerQuestItem(182204317, questId);
		qe.registerOnMovieEndQuest(250, questId);
		for (int npc_id : npc_ids)
			qe.registerQuestNpc(npc_id).addOnTalkEvent(questId);
		qe.registerQuestNpc(700349).addOnKillEvent(questId);
	}

	@Override
	public boolean onZoneMissionEndEvent(QuestEnv env) {
		return defaultOnZoneMissionEndEvent(env);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		return defaultOnLvlUpEvent(env, 2500, true);
	}

	@Override
	public boolean onDialogEvent(final QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null)
			return false;
		int var = qs.getQuestVarById(0);
		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 204774) { // Tristran
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 10002);
				else
					return sendQuestEndDialog(env);
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 204774) { // Tristran
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 0)
							return sendQuestDialog(env, 1011);
					case SETPRO1: {
						giveQuestItem(env, 164000233, 1);
						playQuestMovie(env, 249);
						return defaultCloseDialog(env, 0, 1); // 1
					}
				}
			} else if (targetId == 204809) { // Stua
				switch (env.getDialog()) {
					case QUEST_SELECT:
						if (var == 1)
							return sendQuestDialog(env, 1352);
					case SETPRO2:
						if (var == 1) {
							if (!giveQuestItem(env, 182204317, 1))
								return false;
							QuestService.questTimerStart(env, 240);
							SkillEngine.getInstance().applyEffectDirectly(1865, player, player, (350 * 1000));
							return defaultCloseDialog(env, 1, 2); // 2
						}
				}
			} else if (targetId == 700359 && var == 2) { // Secret Port Entrance
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					QuestService.questTimerEnd(env);
					TeleportService2.teleportTo(player, player.getWorldId(), player.getInstanceId(), 2452, 2474, 672.25f, (byte) 28);
					return playQuestMovie(env, 250);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onMovieEndEvent(QuestEnv env, int movieId) {
		if (movieId != 250)
			return false;
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			player.getEffectController().removeEffect(1865);
			changeQuestStep(env, 2, 3, false); // 3
			return true;
		}
		return false;
	}

	@Override
	public boolean onKillEvent(QuestEnv env) {
		return defaultOnKillEvent(env, 700349, 3, 4); // 4
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		if (item.getItemId() != 182204317)
			return HandlerResult.UNKNOWN;
		if (player.isInsideZone(ZoneName.get("DF3_ITEMUSEAREA_Q2058"))) {
			return HandlerResult.fromBoolean(useQuestItem(env, item, 4, 4, true, 251)); // reward
		}
		return HandlerResult.FAILED;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() != 320110000) {
				int var = qs.getQuestVarById(0);
				if (var == 3) {
					qs.setQuestVar(1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId)
						.getName()));
					return true;
				}
			}
		}
		return false;
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				player.getEffectController().removeEffect(1865);
				qs.setQuestVar(1);
				updateQuestStatus(env);
				return true;
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
			if (var == 2) {
				player.getEffectController().removeEffect(1865);
				qs.setQuestVar(1);
				updateQuestStatus(env);
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onLogOutEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 2) {
				player.getEffectController().removeEffect(1865);
				qs.setQuestVar(1);
				updateQuestStatus(env);
			}
		}
		return false;
	}
}
