package ai.instance.tallocsHollow;

import java.util.List;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.model.templates.spawns.SpawnTemplate;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldMapInstance;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("celestius")
public class CelestiusAI extends AggressiveNpcAI {

	private AtomicBoolean isHome = new AtomicBoolean(true);
	private Future<?> helpersTask;

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (isHome.compareAndSet(true, false)) {
			startHelpersCall();

		}
	}

	private void cancelHelpersTask() {
		if (helpersTask != null && !helpersTask.isDone()) {
			helpersTask.cancel(true);
		}
	}

	private void startHelpersCall() {
		helpersTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable() {

			@Override
			public void run() {
				if (isDead() && getLifeStats().getHpPercentage() < 90) {
					deleteHelpers();
					cancelHelpersTask();
				} else {
					deleteHelpers();
					SkillEngine.getInstance().getSkill(getOwner(), 18981, 44, getOwner()).useNoAnimationSkill();
					startRun((Npc) spawn(281514, 518, 813, 1378, (byte) 0), "3001900001");
					startRun((Npc) spawn(281514, 551, 795, 1376, (byte) 0), "3001900002");
					startRun((Npc) spawn(281514, 574, 854, 1375, (byte) 0), "3001900003");
				}
			}

		}, 1000, 25000);
	}

	private void startRun(Npc npc, String walkId) {
		npc.getSpawn().setWalkerId(walkId);
		WalkManager.startWalking((NpcAI) npc.getAi());
		npc.setState(CreatureState.ACTIVE, true);
		PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.START_EMOTE2, 0, npc.getObjectId()));
	}

	private void deleteHelpers() {
		WorldPosition p = getPosition();
		if (p != null) {
			WorldMapInstance instance = p.getWorldMapInstance();
			if (instance != null) {
				List<Npc> npcs = instance.getNpcs(281514);
				for (Npc npc : npcs) {
					if (npc == null)
						continue;
					SpawnTemplate template = npc.getSpawn();
					if (template.getX() == 518 || template.getX() == 551 || template.getX() == 574) {
						npc.getController().delete();
					}
				}
			}
		}
	}

	@Override
	protected void handleBackHome() {
		cancelHelpersTask();
		deleteHelpers();
		isHome.set(true);
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		cancelHelpersTask();
		deleteHelpers();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cancelHelpersTask();
		deleteHelpers();
		super.handleDied();
	}

}
