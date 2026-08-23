package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNpcAI;

/**
 * @author Luzien, w4terbomb
 */
@AIName("mage_preceptor")
public class MagePreceptorAI extends AggressiveNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 75, 50, 25);

	public MagePreceptorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDespawned() {
		despawnNpcs();
		super.handleDespawned();
	}

	@Override
	protected void handleDied() {
		despawnNpcs();
		int msgId = getNpcId() == 217580 ? 1500216 : 1500218;
		PacketSendUtility.broadcastMessage(getOwner(), msgId);
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		despawnNpcs();
		super.handleBackHome();
		hpPhases.reset();
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 100 -> {
				int msgId = getNpcId() == 217580 ? 1500215 : 1500217;
				PacketSendUtility.broadcastMessage(getOwner(), msgId);
			}
			case 75 -> getOwner().queueSkill(19605, 10, -1, NpcSkillTargetAttribute.RANDOM);
			case 50 -> {
				getOwner().queueSkill(19606, 10, 3000);
				getOwner().queueSkill(19609, 10);
				ThreadPoolManager.getInstance().schedule(this::spawnNpcs, 7500);
			}
			case 25 -> {
				getOwner().queueSkill(19606, 10, 3000);
				getOwner().queueSkill(19605, 10, 6000, NpcSkillTargetAttribute.RANDOM);
				getOwner().queueSkill(19605, 10, 6000, NpcSkillTargetAttribute.RANDOM);
				getOwner().queueSkill(19605, 10, -1, NpcSkillTargetAttribute.RANDOM);
			}
		}
	}

	private void spawnNpcs() {
		if (isDead())
			return;
		WorldPosition p = getPosition();
		spawn(282364, p.getX(), p.getY(), p.getZ(), p.getHeading());
		spawn(282363, p.getX(), p.getY(), p.getZ(), p.getHeading());
		ThreadPoolManager.getInstance().schedule(() -> getOwner().queueSkill(19605, 10, -1, NpcSkillTargetAttribute.RANDOM), 2000);
	}

	private void despawnNpcs() {
		for (Npc npc : getPosition().getWorldMapInstance().getNpcs(282363, 282364))
			npc.getController().delete();
	}
}
