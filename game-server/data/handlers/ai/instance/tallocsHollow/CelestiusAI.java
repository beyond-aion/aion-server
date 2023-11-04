package ai.instance.tallocsHollow;

import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.Summon;
import com.aionemu.gameserver.model.gameobjects.player.Player;
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

	private static final int SUMMONS_ID = 281514;
	private final AtomicBoolean isSpawnTaskStarted = new AtomicBoolean();
	private Future<?> helpersTask;

	public CelestiusAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if ((creature instanceof Player || creature instanceof Summon) && isSpawnTaskStarted.compareAndSet(false, true))
			startHelpersCall();
	}

	private void startHelpersCall() {
		helpersTask = ThreadPoolManager.getInstance().scheduleAtFixedRate(() -> {
			if (!isDead()) {
				SkillEngine.getInstance().getSkill(getOwner(), 18981, 44, getOwner()).useNoAnimationSkill();
				startWalker((Npc) spawn(SUMMONS_ID, 518, 813, 1378, (byte) 0), "3001900001");
				startWalker((Npc) spawn(SUMMONS_ID, 551, 795, 1376, (byte) 0), "3001900002");
				startWalker((Npc) spawn(SUMMONS_ID, 574, 854, 1375, (byte) 0), "3001900003");
			}
		}, 1000, 25000);
	}

	private void startWalker(Npc npc, String walkId) {
		npc.getSpawn().setWalkerId(walkId);
		WalkManager.startWalking((NpcAI) npc.getAi());
		npc.setState(CreatureState.ACTIVE, true);
		PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
	}

	private void cleanUp() {
		cancelTask();
		deleteSummons();
	}

	private void cancelTask() {
		if (helpersTask != null && !helpersTask.isDone()) {
			helpersTask.cancel(true);
		}
	}

	private void deleteSummons() {
		getPosition().getWorldMapInstance().getNpcs(SUMMONS_ID).forEach(npc -> npc.getController().delete());
	}

	@Override
	protected void handleBackHome() {
		cleanUp();
		super.handleBackHome();
	}

	@Override
	protected void handleDespawned() {
		cleanUp();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		cleanUp();
		super.handleDied();
	}

}
