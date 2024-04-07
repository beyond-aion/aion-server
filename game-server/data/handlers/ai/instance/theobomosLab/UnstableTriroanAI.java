package ai.instance.theobomosLab;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * @author Ritsu
 */
@AIName("triroan")
public class UnstableTriroanAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(99, 90, 80, 70, 60, 50, 40, 30, 20, 10, 5);

	public UnstableTriroanAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (hpPhases.getCurrentPhase() > 0 && getLifeStats().isFullyRestoredHp())
			hpPhases.reset();
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 99 -> SkillEngine.getInstance().getSkill(getOwner(), 16699, 1, getOwner()).useSkill();
			case 90 -> spawnFire();
			case 80 -> spawnWater();
			case 70 -> spawnEarth();
			case 60 -> spawnWind();
			case 50 -> spawnFire();
			case 40 -> {
				spawnFire();
				spawnWater();
			}
			case 30 -> {
				spawnEarth();
				spawnWind();
			}
			case 20 -> {
				spawnWind();
				spawnFire();
			}
			case 10 -> {
				spawnWater();
				spawnEarth();
			}
			case 5 -> {
				spawnWind();
				spawnFire();
				spawnWater();
				spawnEarth();
			}
		}
	}

	private void spawnFire() {
		startWalk((Npc) spawn(280975, 601.966f, 488.853f, 196.019f, (byte) 0), "3101100002");
	}

	private void spawnWater() {
		startWalk((Npc) spawn(280976, 601.966f, 488.853f, 196.019f, (byte) 0), "3101100003");
	}

	private void spawnEarth() {
		startWalk((Npc) spawn(280977, 601.966f, 488.853f, 196.019f, (byte) 0), "3101100004");
	}

	private void spawnWind() {
		startWalk((Npc) spawn(280978, 601.966f, 488.853f, 196.019f, (byte) 0), "3101100005");
	}

	private void startWalk(Npc npc, String walkId) {
		npc.getSpawn().setWalkerId(walkId);
		WalkManager.startWalking((NpcAI) npc.getAi());
		npc.setState(CreatureState.ACTIVE, true);
		PacketSendUtility.broadcastPacket(npc, new SM_EMOTION(npc, EmotionType.CHANGE_SPEED, 0, npc.getObjectId()));
	}

	@Override
	protected void handleBackHome() {
		super.handleBackHome();
		hpPhases.reset();
	}
}
