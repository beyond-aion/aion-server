package ai.instance.pvpArenas;

import java.util.List;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;
import com.aionemu.gameserver.model.instance.instancereward.PvPArenaReward;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.ShifterAI;

/**
 * @author xTz
 */
@AIName("plaza_flame_thrower")
public class PlazaFlameThrowerAI extends ShifterAI {

	private boolean isRewarded;

	@Override
	protected void handleDialogStart(Player player) {
		InstanceReward<?> instance = getPosition().getWorldMapInstance().getInstanceHandler().getInstanceReward();
		if (instance != null && !instance.isStartProgress()) {
			return;
		}
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		super.handleUseItemFinish(player);
		if (!isRewarded) {
			isRewarded = true;
			AIActions.handleUseItemFinish(this, player);
			switch (getNpcId()) {
				case 701169:
					useSkill(getNpcs(701178));
					useSkill(getNpcs(701192));
					break;
				case 701170:
					useSkill(getNpcs(701177));
					useSkill(getNpcs(701191));
					break;
				case 701171:
					useSkill(getNpcs(701176));
					useSkill(getNpcs(701190));
					break;
				case 701172:
					useSkill(getNpcs(701175));
					useSkill(getNpcs(701189));
					break;
			}
			AIActions.scheduleRespawn(this);
			AIActions.deleteOwner(this);
		}
	}

	private void useSkill(List<Npc> npcs) {
		PvPArenaReward instance = (PvPArenaReward) getPosition().getWorldMapInstance().getInstanceHandler().getInstanceReward();
		for (Npc npc : npcs) {
			int skill = instance.getNpcBonusSkill(npc.getNpcId());
			SkillEngine.getInstance().getSkill(npc, skill >> 8, skill & 0xFF, npc).useNoAnimationSkill();
		}
	}

	private List<Npc> getNpcs(int npcId) {
		return getPosition().getWorldMapInstance().getNpcs(npcId);
	}
}
