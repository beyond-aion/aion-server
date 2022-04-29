package ai.instance.tallocsHollow;

import java.util.concurrent.Future;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("celestius")
public class CelestiusAI extends AggressiveNpcAI {

	private Future<?> helpersTask;

	public CelestiusAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (helpersTask == null)
			startHelpersCall();
	}

	private synchronized void cancelHelpersTask() {
		if (helpersTask != null && !helpersTask.isDone()) {
			helpersTask.cancel(true);
			helpersTask = null;
		}
	}

	private synchronized void startHelpersCall() {
		if (helpersTask != null)
			return;
		helpersTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead()) {
				SkillEngine.getInstance().getSkill(getOwner(), 18981, 44, getOwner()).useNoAnimationSkill();
				startRun((Npc) spawn(281514, 518, 813, 1378, (byte) 0), "3001900001");
				startRun((Npc) spawn(281514, 551, 795, 1376, (byte) 0), "3001900002");
				startRun((Npc) spawn(281514, 574, 854, 1375, (byte) 0), "3001900003");
			}
		}, 1000, 25000);
	}

	private void startRun(Npc npc, String walkId) {
		npc.getSpawn().setWalkerId(walkId);
		WalkManager.startWalking((NpcAI) npc.getAi());
		npc.setState(CreatureState.ACTIVE, true);
		PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
	}

	private void deleteHelpers() {
		for (Npc npc : getPosition().getWorldMapInstance().getNpcs(281514)) {
			npc.getController().delete();
		}
	}

	@Override
	protected void handleBackHome() {
		cancelHelpersTask();
		deleteHelpers();
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
