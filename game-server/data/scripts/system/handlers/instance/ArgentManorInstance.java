package instance;

import com.aionemu.gameserver.ai2.NpcAI2;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.instance.handlers.GeneralInstanceHandler;
import com.aionemu.gameserver.instance.handlers.InstanceID;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.services.NpcShoutsService;
import com.aionemu.gameserver.skillengine.SkillEngine;

/**
 * @author xTz
 */
@InstanceID(300270000)
public class ArgentManorInstance extends GeneralInstanceHandler {

	@Override
	public void onDie(Npc npc) {
		switch (npc.getNpcId()) {
			case 217243:
				Npc prison = instance.getNpc(205498);
				if (prison != null) {
					NpcShoutsService.getInstance().sendMsg(prison, 1500263, prison.getObjectId(), 0, 0);
					prison.getSpawn().setWalkerId("69B73541CCBF9F7BAB484BA68FF4BE0D2A9B6AD6");
					WalkManager.startWalking((NpcAI2) prison.getAi2());
				}
				spawn(701011, 955.91956f, 1240.153f, 54.090305f, (byte) 90);
				break;
		}
	}

	@Override
	public void handleUseItemFinish(Player player, Npc npc) {
		switch(npc.getNpcId()) {
			case 701001:
				SkillEngine.getInstance().getSkill(npc, 19316, 60, player).useNoAnimationSkill();
				break;
			case 701002:
				SkillEngine.getInstance().getSkill(npc, 19317, 60, player).useNoAnimationSkill();
				break;
			case 701003:
				SkillEngine.getInstance().getSkill(npc, 19318, 60, player).useNoAnimationSkill();
				break;
			case 701004:
				SkillEngine.getInstance().getSkill(npc, 19319, 60, player).useNoAnimationSkill();
				break;
		}
	}

	@Override
	public void onPlayerLogOut(Player player) {
		removeEffects(player);
	}

	@Override
	public void onLeaveInstance(Player player) {
		removeEffects(player);
	}

	private void removeEffects(Player player) {
		player.getEffectController().removeEffect(19316);
		player.getEffectController().removeEffect(19317);
		player.getEffectController().removeEffect(19318);
		player.getEffectController().removeEffect(19319);
	}
}