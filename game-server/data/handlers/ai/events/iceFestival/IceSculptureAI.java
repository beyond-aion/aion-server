package ai.events.iceFestival;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.skillengine.SkillEngine;

import ai.ActionItemNpcAI;

@AIName("icefestival_ice_sculpture")
public class IceSculptureAI extends ActionItemNpcAI {

	public IceSculptureAI(Npc owner) {
		super(owner);
	}

	@Override
	protected void handleUseItemFinish(Player player) {
		super.handleUseItemFinish(player);
		if (Rnd.chance() < 66)
			SkillEngine.getInstance().getSkill(getOwner(), 11020, 60, player).useSkill(); // world_event_icebuff_stat
		else
			SkillEngine.getInstance().getSkill(getOwner(), 22718, 60, player).useWithoutPropSkill(); // world_event_npc_frozen
	}
}
