package ai.instance.pvpArenas;

import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancescore.HarmonyArenaScore;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.ShifterAI;

/**
 * @author xTz
 */
@AIName("harmony_shifter")
public class HarmonyShifterAI extends ShifterAI {

	private AtomicBoolean isRewarded = new AtomicBoolean(false);

	public HarmonyShifterAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		super.handleUseItemFinish(player);
		if (isRewarded.compareAndSet(false, true)) {
			AIActions.handleUseItemFinish(this, player);
			switch (getNpcId()) {
				case 207116:
					useSkill(getNpcs(207118));
					useSkill(getNpcs(207119));
					break;
				case 207099:
					useSkill(getNpcs(207100));
					break;
			}
			AIActions.scheduleRespawn(this);
			AIActions.deleteOwner(this);
		}
	}

	private void useSkill(List<Npc> npcs) {
		HarmonyArenaScore instance = (HarmonyArenaScore) getPosition().getWorldMapInstance().getInstanceHandler().getInstanceScore();
		for (Npc npc : npcs) {
			int skill = instance.getNpcBonusSkill(npc.getNpcId());
			SkillEngine.getInstance().getSkill(npc, skill >> 8, skill & 0xFF, npc).useNoAnimationSkill();
		}
	}

	private List<Npc> getNpcs(int npcId) {
		return getPosition().getWorldMapInstance().getNpcs(npcId);
	}
}
