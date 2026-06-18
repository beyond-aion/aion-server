package ai.instance.empyreanCrucible;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.npcskill.NpcSkillTargetAttribute;
import com.aionemu.gameserver.utils.PacketSendUtility;
import com.aionemu.gameserver.utils.ThreadPoolManager;
import com.aionemu.gameserver.world.WorldPosition;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Luzien, w4terbomb
 */
@AIName("priest_preceptor")
public class PriestPreceptorAI extends AggressiveNoLootNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(100, 75, 25);
	private final int[] helpers;

	public PriestPreceptorAI(Npc owner) {
		super(owner);
		if (owner.getNpcId() == 217581) // Thrasymedes
			helpers = new int[] { 282366, 282367, 282368 }; // Boreas, Jumentis, Charna
		else // Freyr
			helpers = new int[] { 282369, 282370, 282371 }; // Traufnir, Sigyn, Sif
	}

	protected void handleSpawned() {
		super.handleSpawned();
		ThreadPoolManager.getInstance().schedule(() -> getOwner().queueSkill(19612, 10, -1, NpcSkillTargetAttribute.ME), 1000);
	}

	@Override
	protected void handleDied() {
		despawnHelpers();
		int msgId = getNpcId() == 217581 ? 1500220 : 1500222;
		PacketSendUtility.broadcastMessage(getOwner(), msgId);
		super.handleDied();
	}

	@Override
	protected void handleBackHome() {
		despawnHelpers();
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
				int msgId = getNpcId() == 217581 ? 1500219 : 1500221;
				PacketSendUtility.broadcastMessage(getOwner(), msgId);
			}
			case 75 -> getOwner().queueSkill(19611, 10, -1, NpcSkillTargetAttribute.RANDOM); // Word of Destruction II
			case 25 -> startTask();
		}
	}

	private void startTask() {
		getOwner().queueSkill(19610, 10, 2000);
		getOwner().queueSkill(19614, 10, -1, NpcSkillTargetAttribute.ME);
		ThreadPoolManager.getInstance().schedule(() -> {
			WorldPosition p = getPosition();
			for (int helperNpcId : helpers)
				applySoulSickness((Npc) spawn(helperNpcId, p.getX(), p.getY(), p.getZ(), p.getHeading()));
		}, 7000);
	}

	private void applySoulSickness(Npc npc) {
		ThreadPoolManager.getInstance().schedule(() -> npc.queueSkill(19594, 4, -1, NpcSkillTargetAttribute.ME), 1000);
	}

	private void despawnHelpers() {
		getPosition().getWorldMapInstance().getNpcs(helpers).forEach(npc -> npc.getController().deleteIfAliveOrCancelRespawn());
	}
}