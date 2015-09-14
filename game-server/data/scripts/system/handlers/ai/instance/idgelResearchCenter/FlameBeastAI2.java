package ai.instance.idgelResearchCenter;

import java.util.List;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.skillengine.SkillEngine;
import com.aionemu.gameserver.utils.MathUtil;
import com.aionemu.gameserver.world.WorldMapInstance;

/**
 * @author Ritsu
 */
@AIName("flamebeast")
public class FlameBeastAI2 extends AggressiveNpcAI2 {

	@Override
	protected void handleSpawned() {
		super.handleSpawned();
		SkillEngine.getInstance().getSkill(getOwner(), 21121, 30, getOwner()).useWithoutPropSkill();
	}

	@Override
	protected void handleCreatureSee(Creature creature) {
		checkDistance(this, creature);
	}

	@Override
	protected void handleCreatureMoved(Creature creature) {
		checkDistance(this, creature);
	}

	private void checkDistance(NpcAI2 ai, Creature creature) {
		Npc marabata = getPosition().getWorldMapInstance().getNpc(230107);
		if (creature instanceof Npc) {
			if (marabata != null && MathUtil.isIn3dRange(getOwner(), marabata, 8) && marabata.getEffectController().hasAbnormalEffect(21121)) {
				WorldMapInstance instance = getPosition().getWorldMapInstance();
				SkillEngine.getInstance().getSkill(marabata, 21122, 30, marabata).useSkill();
				SkillEngine.getInstance().getSkill(getOwner(), 21122, 30, getOwner()).useSkill();
				deleteNpcs(instance.getNpcs(284642));
			}
		}
	}

	private void deleteNpcs(List<Npc> npcs) {
		for (Npc npc : npcs) {
			if (npc != null)
				npc.getController().onDelete();
		}
	}

}
