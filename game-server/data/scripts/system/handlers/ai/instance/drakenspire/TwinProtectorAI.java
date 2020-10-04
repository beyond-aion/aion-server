package ai.instance.drakenspire;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.skill.QueuedNpcSkillEntry;
import com.aionemu.gameserver.model.templates.npcskill.QueuedNpcSkillTemplate;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.skillengine.model.SkillTemplate;

import ai.AggressiveNpcAI;

/**
 * @author Estrayl
 */
@AIName("twin_protector")
public class TwinProtectorAI extends AggressiveNpcAI {

	private final List<Integer> percents = new ArrayList<>();
	private final List<Npc> adds = new ArrayList<>();

	public TwinProtectorAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleAttack(Creature creature) {
		super.handleAttack(creature);
		checkPercentage(getLifeStats().getHpPercentage());
	}

	private synchronized void checkPercentage(int hpPercentage) {
		for (Integer percent : percents) {
			if (hpPercentage <= percent) {
				percents.remove(percent);
				switch (percent) {
					case 65, 40, 15 -> {
						getOwner().getQueuedSkills().clear();
						getOwner().getQueuedSkills().offer(new QueuedNpcSkillEntry(new QueuedNpcSkillTemplate(21644, 1, 100, 0, 10000))); // Raging Hellfire
					}
					case 25, 10 -> spawnAdds(getNpcId() % 2 == 0 ? 855622 : 855621, 20);
				}
				break;
			}
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

	private void addPercent() {
		percents.clear();
		Collections.addAll(percents, 65, 40, 25, 15, 10);
	}

	private void despawnAdds() {
		for (Npc npc : adds)
			if (npc != null)
				npc.getController().delete();
	}

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		addPercent();
	}

	@Override
	protected void handleDespawned() {
		despawnAdds();
		super.handleDespawned();
	}

	@Override
	protected void handleBackHome() {
		addPercent();
		despawnAdds();
		super.handleBackHome();
	}

}
