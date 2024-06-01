package ai.instance.drakenspire;

import java.util.ArrayList;
import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.ai.HpPhases;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.AggressiveNoLootNpcAI;

/**
 * @author Estrayl
 */
@AIName("twin_protector")
public class TwinProtectorAI extends AggressiveNoLootNpcAI implements HpPhases.PhaseHandler {

	private final HpPhases hpPhases = new HpPhases(65, 40, 25, 15, 10);
	private final List<Npc> adds = new ArrayList<>();

	public TwinProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		hpPhases.tryEnterNextPhase(this);
	}

	@Override
	public void handleHpPhase(int phaseHpPercent) {
		switch (phaseHpPercent) {
			case 65, 40, 15 -> {
				getOwner().clearQueuedSkills();
				getOwner().queueSkill(21644, 1, 10000); // Raging Hellfire
			}
			case 25, 10 -> spawnAdds(getNpcId() % 2 == 0 ? 855622 : 855621, 20);
		}
	}

	@Override
	public void onEndUseSkill(SkillTemplate skillTemplate, int skillLevel) {
		if (skillTemplate.getSkillId() == 21644) { // Raging Hellfire
			SkillEngine.getInstance().getSkill(getOwner(), 21645, 1, getTarget()).useNoAnimationSkill();
			spawnAdds(855625, 50);
		}
	}

	private void spawnAdds(int npcId, int hpThreshold) {
		int count = getLifeStats().getHpPercentage() < hpThreshold ? 3 : 1;
		for (Player p : getKnownList().getKnownPlayers().values()) {
			if (p != null && !p.isDead() && isInRange(p, 20)) {
				adds.add((Npc) spawn(npcId, p.getX(), p.getY(), p.getZ(), (byte) 0));
				count--;
				if (count <= 0)
					break;
			}
		}
	}

	private void despawnAdds() {
		for (Npc npc : adds)
			if (npc != null)
				npc.getController().delete();
	}

	@Override
	protected void handleDespawned() {
		despawnAdds();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		despawnAdds();
		super.handleBackHome();
		hpPhases.reset();
	}
}
