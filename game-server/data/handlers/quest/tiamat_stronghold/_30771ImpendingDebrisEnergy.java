package quest.tiamat_stronghold;

import static com.aionemu.gameserver.model.DialogAction.*;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.geoEngine.math.Vector3f;
import com.aionemu.gameserver.model.gameobjects.Item;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.questEngine.handlers.AbstractQuestHandler;
import com.aionemu.gameserver.questEngine.handlers.HandlerResult;
import com.aionemu.gameserver.questEngine.model.QuestEnv;
import com.aionemu.gameserver.questEngine.model.QuestState;
import com.aionemu.gameserver.questEngine.model.QuestStatus;
import com.aionemu.gameserver.spawnengine.SpawnEngine;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;
import com.aionemu.gameserver.world.geo.GeoService;
import com.aionemu.gameserver.world.zone.ZoneName;

/**
 * While using the restorative the player should be ambushed by some drakans.
 *
 * @author Estrayl
 */
public class _30771ImpendingDebrisEnergy extends AbstractQuestHandler {

	private static final int QUEST_ITEM_ID = 182215699; // Restorative
	private static final int START_NPC_ID = 804728; // Engrid
	private static final int TALK_NPC_1_ID = 804871; // Hank
	private static final int TALK_NPC_2_ID = 804869; // Ginnie
	private static final int ATTACKER_NPC_ID = 217424; // Mirror Image

	public _30771ImpendingDebrisEnergy() {
		super(30771);
	}

	@Override
	public void register() {
		qe.registerQuestNpc(START_NPC_ID).addOnQuestStart(questId);
		qe.registerQuestNpc(TALK_NPC_1_ID).addOnTalkEvent(questId);
		qe.registerQuestNpc(TALK_NPC_2_ID).addOnTalkEvent(questId);
		qe.registerQuestItem(QUEST_ITEM_ID, questId);
	}

	public boolean onDialogEvent(QuestEnv env) {
		QuestState qs = env.getPlayer().getQuestStateList().getQuestState(questId);
		int dialogActionId = env.getDialogActionId();
		int targetId = env.getTargetId();

		if (qs == null || qs.isStartable()) {
			if (targetId == START_NPC_ID) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 4762);
				} else {
					return sendQuestStartDialog(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.START) {
			int var = qs.getQuestVarById(0);
			if (targetId == TALK_NPC_1_ID && var == 0) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 1011);
				} else if (dialogActionId == SETPRO1) {
					changeQuestStep(env, 0, 1);
					return closeDialogWindow(env);
				}
			} else if (targetId == TALK_NPC_2_ID) {
				if (dialogActionId == QUEST_SELECT) {
					if (var == 1) {
						return sendQuestDialog(env, 1352);
					} else if (var == 3) {
						return sendQuestDialog(env, 2034);
					}
				} else if (dialogActionId == SETPRO2) {
					changeQuestStep(env, 1, 2);
					giveQuestItem(env, QUEST_ITEM_ID, 1);
					return closeDialogWindow(env);
				} else if (dialogActionId == SET_SUCCEED) {
					changeQuestStep(env, 3, 4, true);
					return closeDialogWindow(env);
				}
			}
		} else if (qs.getStatus() == QuestStatus.REWARD) {
			if (targetId == TALK_NPC_1_ID) {
				if (dialogActionId == QUEST_SELECT) {
					return sendQuestDialog(env, 10002);
				} else {
					return sendQuestEndDialog(env);
				}
			}
		}
		return false;
	}

	@Override
	public HandlerResult onItemUseEvent(final QuestEnv env, Item item) {
		Player player = env.getPlayer();
		QuestState qs = player.getQuestStateList().getQuestState(questId);
		if (qs != null && qs.getStatus() == QuestStatus.START) {
			if (player.isInsideItemUseZone(ZoneName.get("DF5_ITEMUSEAREA_Q30771"))) {
				int var = qs.getQuestVarById(0);
				if (var == 2) {
					boolean isItemUseComplete = useQuestItem(env, item, 2, 3, false);
					if (isItemUseComplete) {
						ThreadPoolManager.getInstance().schedule(() -> {
							rndSpawnInRange(player, Rnd.nextFloat(4f));
							rndSpawnInRange(player, Rnd.nextFloat(4f));
						}, 1500);
					}
					return HandlerResult.fromBoolean(isItemUseComplete);
				}
			}
		}
		return HandlerResult.FAILED;
	}

	private void rndSpawnInRange(Player player, float distance) {
		WorldPosition p = player.getPosition();
		double angleRadians = Math.toRadians(Rnd.nextFloat(360f));
		float x = p.getX() + (float) (Math.cos(angleRadians) * distance);
		float y = p.getY() + (float) (Math.sin(angleRadians) * distance);

		Vector3f pos = GeoService.getInstance().getClosestCollision(player, x, y, p.getZ());
		SpawnTemplate template = SpawnEngine.newSingleTimeSpawn(p.getMapId(), ATTACKER_NPC_ID, pos.getX(), pos.getY(), pos.getZ(), (byte) 0);

		Npc npc = (Npc) SpawnEngine.spawnObject(template, p.getInstanceId());
		npc.getAggroList().addHate(player, 1000);
	}
}
