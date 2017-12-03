package ai.instance.pvpArenas;

import com.aionemu.gameserver.ai.AIActions;
import com.aionemu.gameserver.ai.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;

import ai.ActionItemNpcAI;

/**
 * @author xTz
 */
@AIName("pvparenarelics")
public class RelicsAI extends ActionItemNpcAI {

	private boolean isRewarded;

	public RelicsAI(Npc owner) {
		super(owner);
	}

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
		if (!isRewarded) {
			isRewarded = true;
			AIActions.handleUseItemFinish(this, player);
			final int npcId = getNpcId();
			if (npcId != 701187 && npcId != 701188) {
				AIActions.scheduleRespawn(this);
			}
			AIActions.deleteOwner(this);
		}
	}
}
