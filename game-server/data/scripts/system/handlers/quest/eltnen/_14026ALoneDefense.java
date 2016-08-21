package quest.eltnen;

import java.util.Collection;
import java.util.List;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AIState;
import com.aionemu.gameserver.ai2.AbstractAI;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.animations.TeleportAnimation;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.network.aion.serverpackets.SM_DIALOG_WINDOW;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.questEngine.handlers.QuestHandler;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.services.QuestService;
import com.aionemu.gameserver.services.teleport.TeleportService2;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.world.World;
import com.aionemu.gameserver.world.WorldMapType;

import javolution.util.FastTable;

/**
 * @author Artur
 */
public class _14026ALoneDefense extends QuestHandler {

	private static List<Integer> mobs = new FastTable<>();

	static {
		mobs.add(211628);
		mobs.add(211630);
		mobs.add(213575);
	}

	public _14026ALoneDefense() {
		super(14026);
	}

	@Override
	public void register() {
		qe.registerOnQuestCompleted(questId);
		qe.registerOnLevelChanged(questId);
		qe.registerOnDie(questId);
		qe.registerOnLogOut(questId);
		qe.registerOnQuestTimerEnd(questId);
		qe.registerQuestNpc(203901).addOnTalkEvent(questId);
		qe.registerQuestNpc(204020).addOnTalkEvent(questId);
		qe.registerQuestNpc(204044).addOnTalkEvent(questId);
		for (int mob : mobs) {
			qe.registerQuestNpc(mob).addOnKillEvent(questId);
		}
		qe.registerQuestNpc(700141).addOnTalkEvent(questId);
	}

	@Override
	public boolean onDialogEvent(QuestEnv env) {
		final Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);

		int targetId = 0;
		if (env.getVisibleObject() instanceof Npc)
			targetId = ((Npc) env.getVisibleObject()).getNpcId();

		if (qs == null)
			return false;

		if (qs.getStatus() == QuestStatus.START) {
			switch (targetId) {
				case 203901: {
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 0) {
								return sendQuestDialog(env, 1011);
							}
							return false;
						}
						case SETPRO1: {
							qs.setQuestVar(1);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							TeleportService2.teleportTo(player, WorldMapType.ELTNEN.getId(), 1596.1948f, 1529.9152f, 317, (byte) 120,
								TeleportAnimation.FADE_OUT_BEAM);
							return true;
						}
					}
					return false;
				}
				case 204020: {
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							if (qs.getQuestVarById(0) == 1) {
								return sendQuestDialog(env, 1352);
							}
							return false;
						}
						case SETPRO2: {
							qs.setQuestVar(2);
							updateQuestStatus(env);
							giveQuestItem(env, 182215324, 1);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							TeleportService2.teleportTo(player, WorldMapType.ELTNEN.getId(), 2500.15f, 780.9f, 409, (byte) 15, TeleportAnimation.FADE_OUT_BEAM);
							return true;
						}
					}
					return false;
				}
				case 204044: {
					switch (env.getDialog()) {
						case QUEST_SELECT: {
							switch (qs.getQuestVarById(0)) {
								case 2: {
									return sendQuestDialog(env, 1693);
								}
								case 4: {
									return sendQuestDialog(env, 2034);
								}
							}
							return false;
						}
						case SETPRO3: {
							qs.setQuestVar(3);
							updateQuestStatus(env);
							PacketSendUtility.sendPacket(player, new SM_DIALOG_WINDOW(env.getVisibleObject().getObjectId(), 10));
							QuestService.questTimerStart(env, 180);
							spawn(player);
							return true;
						}
						case SETPRO4: {
							qs.setStatus(QuestStatus.REWARD);
							updateQuestStatus(env);
							TeleportService2.teleportTo(player, WorldMapType.ELTNEN.getId(), 271.69f, 2787.04f, 272.47f, (byte) 50,
								TeleportAnimation.FADE_OUT_BEAM);
							return true;
						}
					}
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == 203901) {
				switch (env.getDialog()) {
					case QUEST_SELECT:
						return sendQuestDialog(env, 2375);
					default:
						return sendQuestEndDialog(env, 0);
				}
			}
		}
		return false;
	}

	@Override
	public void onQuestCompletedEvent(QuestEnv env) {
		int[] quests = { 14020, 14021, 14022, 14023, 14024, 14025 };
		defaultOnQuestCompletedEvent(env, quests);
	}

	@Override
	public void onLevelChangedEvent(Player player) {
		int[] quests = { 14020, 14021, 14022, 14023, 14024, 14025 };
		defaultOnLevelChangedEvent(player, quests);
	}

	@Override
	public boolean onQuestTimerEndEvent(QuestEnv env) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (var == 3) {
				qs.setQuestVar(4);
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
			if (var == 3) {
				changeQuestStep(env, var, 2);
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
			if (var == 3) {
				changeQuestStep(env, var, 2);
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
			int var = qs.getQuestVarById(0);
			if (var == 3) {
				int targetId = env.getTargetId();
				if (mobs.contains(targetId)) {
					spawn(player);
					return true;
				}
			}
		}
		return false;
	}

	private void spawn(Player player) {
		int mobToSpawn = mobs.get(Rnd.get(0, 2));
		float x = 0;
		float y = 0;
		final float z = 217.48f;
		switch (mobToSpawn) {
			case 211628: {
				x = 254.74f;
				y = 236.72f;
				break;
			}
			case 211630: {
				x = 257.92f;
				y = 237.39f;
				break;
			}
			case 213575: {
				x = 261.86f;
				y = 237.5f;
				break;
			}
		}
		Npc spawn = (Npc) QuestService.spawnQuestNpc(310040000, player.getInstanceId(), mobToSpawn, x, y, z, (byte) 95);
		Collection<Npc> allNpcs = World.getInstance().getNpcs();
		Npc target = null;
		for (Npc npc : allNpcs) {
			if (npc.getNpcId() == 204044) {
				target = npc;
			}
		}
		if (target != null) {
			spawn.setTarget(target);
			((AbstractAI) spawn.getAi2()).setStateIfNot(AIState.WALKING);
			spawn.setState(1);
			spawn.getMoveController().moveToTargetObject();
			PacketSendUtility.broadcastPacket(spawn, new SM_EMOTION(spawn, EmotionType.START_EMOTE2, 0, spawn.getObjectId()));
		}
	}
}
