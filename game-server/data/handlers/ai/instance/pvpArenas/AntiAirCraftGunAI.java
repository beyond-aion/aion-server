package ai.instance.pvpArenas;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancescore.InstanceScore;
import com.aionemu.gameserver.services.teleport.TeleportService;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.ActionItemNpcAI;

/**
 * @author xTz
 */
@AIName("antiaircraftgun")
public class AntiAirCraftGunAI extends ActionItemNpcAI {

	public AntiAirCraftGunAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleDialogStart(Player player) {
		InstanceScore<?> instance = getPosition().getWorldMapInstance().getInstanceHandler().getInstanceScore();
		if (instance != null && !instance.isStartProgress()) {
			return;
		}
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		Npc owner = getOwner();
		TeleportService.teleportTo(player, owner.getWorldId(), owner.getInstanceId(), owner.getX(), owner.getY(), owner.getZ(), owner.getHeading());
		int morphSkill = 0;
		switch (getNpcId()) {
			case 701185: // 46 lvl morph 218803
			case 701321:
				morphSkill = 0x4E502E; // 20048 46
				break;
			case 701199: // 51 lvl morph 218804
			case 701322:
				morphSkill = 0x4E5133; // 20049 51
				break;
			case 701213: // 56 lvl morph 218805
			case 701323:
				morphSkill = 0x4E5238; // 20050 56
				break;
		}
		SkillEngine.getInstance().getSkill(getOwner(), morphSkill >> 8, morphSkill & 0xFF, player).useNoAnimationSkill();
		AIActions.scheduleRespawn(this);
		AIActions.deleteOwner(this);
	}
}
