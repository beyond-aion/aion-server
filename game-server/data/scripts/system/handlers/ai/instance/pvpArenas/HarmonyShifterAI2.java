package ai.instance.pvpArenas;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.HarmonyArenaReward;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.ShifterAI2;

/**
 * @author xTz
 */
@AIName("harmony_shifter")
public class HarmonyShifterAI2 extends ShifterAI2 {

	private AtomicBoolean isRewarded = new AtomicBoolean(false);

	@Override
	protected void handleUseItemFinish(Player player) {
		super.handleUseItemFinish(player);
		if (isRewarded.compareAndSet(false, true)) {
			AI2Actions.handleUseItemFinish(this, player);
			switch (getNpcId()) {
				case 207116:
					useSkill(getNpcs(207118));
					useSkill(getNpcs(207119));
					break;
				case 207099:
					useSkill(getNpcs(207100));
					break;
			}
			AI2Actions.scheduleRespawn(this);
			AI2Actions.deleteOwner(this);
		}
	}

	private void useSkill(List<Npc> npcs) {
		HarmonyArenaReward instance = (HarmonyArenaReward) getPosition().getWorldMapInstance().getInstanceHandler().getInstanceReward();
		for (Npc npc : npcs) {
			int skill = instance.getNpcBonusSkill(npc.getNpcId());
			SkillEngine.getInstance().getSkill(npc, skill >> 8, skill & 0xFF, npc).useNoAnimationSkill();
		}
	}

	private List<Npc> getNpcs(int npcId) {
		return getPosition().getWorldMapInstance().getNpcs(npcId);
	}
}
