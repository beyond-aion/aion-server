package quest.sarpan;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.DialogAction;
import com.aionemu.gameserver.model.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.SystemMessageId;
import com.aionemu.gameserver.network.aion.serverpackets.SM_PLAY_MOVIE;
import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
import com.aionemu.gameserver.questEngine.QuestEngine;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.instance.InstanceService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.knownlist.Visitor;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * @author zhkchi
 */
public class _10040AssaultOnTiamaranta extends QuestHandler {

	private final static int questId = 10040;

	public _10040AssaultOnTiamaranta() {
		super(questId);
	}

	@Override
	public void register() {
		qe.registerOnEnterWorld(questId);
		qe.registerOnLevelUp(questId);
		qe.registerOnDie(questId);
		qe.registerQuestNpc(798926).addOnQuestStart(questId);
		qe.registerQuestNpc(800084).addOnTalkEvent(questId);
		qe.registerQuestNpc(799721).addOnTalkEvent(questId);
		qe.registerQuestNpc(800280).addOnTalkEvent(questId);
		qe.registerQuestNpc(205535).addOnTalkEvent(questId);
		qe.registerQuestNpc(730527).addOnTalkEvent(questId);
		qe.registerQuestNpc(205818).addOnAttackEvent(questId);
		qe.registerOnLeaveZone(ZoneName.get("EULOS_CAPTAINS_CABIN_300410000"), questId);
		qe.registerOnLeaveZone(ZoneName.get("EULOS_DECK_300410000"), questId);
	}

	@Override
	public boolean onLvlUpEvent(QuestEnv env) {
		if (defaultOnLvlUpEvent(env)) {
			int[] ids = { 10050, 10051, 10052, 10053 };
			for (int id : ids) {
				QuestEngine.getInstance().onEnterZoneMissionEnd(new QuestEnv(env.getVisibleObject(), env.getPlayer(), id, env.getDialogId()));
			}
			return true;
		}
		return false;
	}

	@Override
	public boolean onEnterWorldEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.getWorldId() != 300410000) {
				int var = qs.getQuestVarById(0);
				if (var > 1) {
					qs.setQuestVar(1);
					updateQuestStatus(env);
					PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId)
						.getName()));
					return true;
				}
			}
		} else if (qs != null && qs.getStatus() == QuestStatus.REWARD) {
			if (player.getWorldId() != 600020000) {
				qs.setStatus(QuestStatus.START);
				qs.setQuestVar(1);
				updateQuestStatus(env);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId)
					.getName()));
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		DialogAction dialog = env.getDialog();
		int targetId = env.getTargetId();

		if (qs == null) {
			if (targetId == 798926) {
				if (dialog == DialogAction.QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		}

		if (qs == null)
			return false;

		int var = qs.getQuestVarById(0);

		if (qs.getStatus() == QuestStatus.START) {
			if (targetId == 798926 && var == 0) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1011);
				else if (env.getDialog() == DialogAction.SETPRO1) {
					changeQuestStep(env, 0, 1, false);
					return closeDialogWindow(env);
				} else
					return sendQuestStartDialog(env);
			} else if (targetId == 800084 && var == 1) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1352);
				else if (env.getDialog() == DialogAction.SETPRO2) {
					WorldMapInstance newInstance = InstanceService.getNextAvailableInstance(300410000);
					InstanceService.registerPlayerWithInstance(newInstance, player);
					TeleportService2.teleportTo(player, 300410000, newInstance.getInstanceId(), 772, 211, 1029);
					changeQuestStep(env, 1, 2, false);
					PacketSendUtility.sendPacket(player, new SM_PLAY_MOVIE(1, 18));
					return closeDialogWindow(env);
				} else
					return sendQuestStartDialog(env);
			} else if (targetId == 799721 && var == 2) {
				if (env.getDialog() == DialogAction.QUEST_SELECT)
					return sendQuestDialog(env, 1693);
				else if (env.getDialog() == DialogAction.SELECT_ACTION_1694)
					return sendQuestDialog(env, 1694);
				else if (env.getDialog() == DialogAction.SETPRO3) {
					changeQuestStep(env, 2, 3, false);
					return closeDialogWindow(env);
				} else
					return sendQuestStartDialog(env);
			} else if (targetId == 730527 && var == 3) {
				if (env.getDialog() == DialogAction.USE_OBJECT) {
					TeleportService2.teleportTo(player, 300410000, player.getInstanceId(), 756, 214, 1029, (byte) 116, TeleportAnimation.BEAM_ANIMATION);
					return true;
				} else
					return sendQuestStartDialog(env);
			} else if (targetId == 800280) {
				if (env.getDialog() == DialogAction.USE_OBJECT)
					return sendQuestDialog(env, 2716);
				else if (env.getDialog() == DialogAction.SELECT_ACTION_2717)
					return sendQuestDialog(env, 2717);
				/*
				 * else if (env.getDialog() == DialogAction.SET_SUCCEED) { TeleportService2.teleportTo(player, 600020000, 1216f, 1360f, 1358f, (byte) 0,
				 * TeleportAnimation.BEAM_ANIMATION);
				 */
				else if (env.getDialog() == DialogAction.SET_SUCCEED) {
					qs.setStatus(QuestStatus.REWARD);
					updateQuestStatus(env);
					TeleportService2.teleportTo(player, 600020000, 1, 1219, 1361, 1359, (byte) 70, TeleportAnimation.BEAM_ANIMATION);
					return closeDialogWindow(env);
				} else
					return sendQuestStartDialog(env);
			}
			return false;
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 205535) {
				if (dialog == DialogAction.USE_OBJECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public boolean onAttackEvent(final QuestEnv env) {
		final Player player = env.getPlayer();
		final QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVars().getQuestVars();
			if (var == 3) {
				int targetId = env.getTargetId();

				if (targetId == 205818) {
					qs.setQuestVar(4);

					SkillEngine.getInstance().applyEffectDirectly(20412, player, player, 18000);

					final WorldMapInstance instance = InstanceService.getRegisteredInstance(300410000, player.getObjectId());
					for (Npc npc : instance.getNpcs()) {
						switch (npc.getNpcId()) {
							case 800280:
							case 800295:
							case 800296:
							case 800294:
							case 800297:
							case 800298:
								break;
							default:
								SkillEngine.getInstance().applyEffectDirectly(20412, npc, npc, 18000);
						}
					}

					messadges(env);
					ThreadPoolManager.getInstance().schedule(new Runnable() {

						@Override
						public void run() {

							for (Npc npc : instance.getNpcs()) {
								switch (npc.getNpcId()) {
									case 800280:
									case 800295:
									case 800296:
									case 800294:
									case 800297:
									case 800298:
										npc.getEffectController().removeAllEffects();

										break;
									default:
										npc.getController().onDelete();
								}
							}
							qs.setQuestVar(11);
							updateQuestStatus(env);
						}
					}, 18000);
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
			if (var > 1) {
				qs.setQuestVar(1);
				updateQuestStatus(env);
				PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(SystemMessageId.QUEST_FAILED_$1, DataManager.QUEST_DATA.getQuestById(questId)
					.getName()));
				return true;
			}
		}
		return false;
	}

	private void messadges(QuestEnv env) {
		for (Npc npc : World.getInstance().getNpcs()) {
			final int npcId = npc.getNpcId();
			final int objectId = npc.getObjectId();
			switch (npcId) {
				case 800280:
					sendMsg(1111387, objectId, true, 1000);
					sendMsg(1111390, objectId, true, 12000);
					sendMsg(1111391, objectId, true, 18000);
					break;
				case 800294:
					sendMsg(1111388, objectId, true, 4000);
					break;
				case 800298:
					sendMsg(1111389, objectId, true, 8000);
					break;
				case 800297:
					sendMsg(1111392, objectId, true, 15000);
					break;
			}
		}
	}

	private void sendMsg(final int msg, final int Obj, final boolean isShout, int time) {
		ThreadPoolManager.getInstance().schedule(new Runnable() {

			@Override
			public void run() {
				World.getInstance().doOnAllPlayers(new Visitor<Player>() {

					@Override
					public void visit(Player player) {
						if (player.isOnline()) {
							PacketSendUtility.sendPacket(player, new SM_SYSTEM_MESSAGE(isShout, msg, Obj, 0));
						}
					}
				});
			}

		}, time);
	}

	@Override
	public boolean onLeaveZoneEvent(QuestEnv env, ZoneName zoneName) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs == null || qs.getStatus() != QuestStatus.START)
			return false;
		if (zoneName.equals(ZoneName.get("EULOS_CAPTAINS_CABIN_300410000"))) {
			if (qs.getQuestVarById(0) < 3) {
				qs.setQuestVarById(0, 1);
				return true;
			}
		} else if (zoneName.equals(ZoneName.get("EULOS_DECK_300410000"))) {
			qs.setQuestVarById(0, 1);
			return true;
		}
		return false;
	}
}
