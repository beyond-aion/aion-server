package ai.instance.idgelResearchCenter;

import ai.ActionItemNpcAI2;

import com.aionemu.commons.utils.Rnd;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.gameobjects.player.Player;
import com.aionemu.gameserver.model.instance.instancereward.InstanceReward;

/**
 *
 * @author Ritsu
 */
@AIName("suspiciouspot")
public class SuspiciousPotAI2 extends ActionItemNpcAI2 
{

	private boolean isSpawned;

	@Override
	protected void handleDialogStart(Player player)
	{
		InstanceReward<?> instance = getPosition().getWorldMapInstance().getInstanceHandler().getInstanceReward();
		if (instance != null && !instance.isStartProgress()) {
			return;
		}
		super.handleDialogStart(player);
	}

	@Override
	protected void handleUseItemFinish(Player player) 
	{
		if (!isSpawned) 
		{
			isSpawned = true;
			AI2Actions.handleUseItemFinish(this, player);
			getOwner().getController().die();
			Npc npc = getOwner();
			float direction = Rnd.get(0, 199) / 100f;
			int distance = Rnd.get(1, 2);
			float x1 = (float) (Math.cos(Math.PI * direction) * distance);
			float y1 = (float) (Math.sin(Math.PI * direction) * distance);
			for (int i = 0; i < 5; i++)
			{
				spawn(230118,  npc.getX() + x1,  npc.getY() + y1,  npc.getZ(),  (byte) 0);
			}

		}
	}
}
