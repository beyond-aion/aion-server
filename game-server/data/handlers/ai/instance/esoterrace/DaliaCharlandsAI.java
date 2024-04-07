package ai.instance.esoterrace;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.ai.NpcAI;
import com.aionemu.gameserver.ai.manager.WalkManager;
import com.aionemu.gameserver.model.EmotionType;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.state.CreatureState;
import com.aionemu.gameserver.network.aion.serverpackets.SM_EMOTION;
import com.aionemu.gameserver.utils.PacketSendUtility;

import ai.AggressiveNpcAI;

/**
 * @author xTz
 */
@AIName("dalia_charlands")
public class DaliaCharlandsAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(75, 50, 25);

	public DaliaCharlandsAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		if (hpPhases.getCurrentPhase() > 0 && getLifeStats().getHpPercentage() > 80)
			hpPhases.reset();
		else
			hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 75:
			case 50:
			case 25:
				spawnHelpers();
				break;
		}
	}

	private void spawnHelpers() {
		startWalk((Npc) spawn(282177, 1173.68f, 674.11f, 297.5f, (byte) 0), "3002500001");
		startWalk((Npc) spawn(282176, 1174.44f, 669.64f, 297.5f, (byte) 0), "3002500002");
		startWalk((Npc) spawn(282178, 1176.2f, 677.32f, 297.5f, (byte) 0), "3002500003");
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
