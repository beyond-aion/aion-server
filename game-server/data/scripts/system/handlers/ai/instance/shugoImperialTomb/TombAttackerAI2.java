package ai.instance.shugoImperialTomb;


import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.manager.EmoteManager;
import com.aionemu.gameserver.ai2.manager.WalkManager;
import com.aionemu.gameserver.dataholders.DataManager;
import com.aionemu.gameserver.model.gameobjects.Npc;
import com.aionemu.gameserver.model.templates.walker.WalkerTemplate;

/**
 * @author Ritsu
 */
@AIName("tombattacker")
public class TombAttackerAI2 extends AggressiveNpcAI2
{
	private WalkerTemplate template;
	private boolean canThink = true;
	private final static int[] npc_ids = { 831251, 831250, 831304, 831305, 831130 };

	private void addHate()
	{
		EmoteManager.emoteStopAttacking(getOwner());
		for (int npc_id : npc_ids)
		{
			Npc tower = getOwner().getPosition().getWorldMapInstance().getNpc(npc_id);
			if (tower != null && !tower.getLifeStats().isAlreadyDead())
			{
				switch (npc_id)
				{
					case 831251:
						getOwner().getAggroList().addHate(tower, 100);
					case 831250:
						getOwner().getAggroList().addHate(tower, 100);
					case 831304:
						getOwner().getAggroList().addHate(tower, 100);
					case 831305:
						getOwner().getAggroList().addHate(tower, 100);
					case 831130:
						getOwner().getAggroList().addHate(tower, 100);
						break;
				}
			}
		}
	}

	@Override
	public int modifyOwnerDamage(int damage) 
	{
		return damage = 1;
	}

	@Override
	public boolean canThink()
	{
		return canThink;
	}

	@Override
	protected void handleSpawned() 
	{
		canThink = false;
		super.handleSpawned();
	}

	@Override
	protected void handleMoveArrived()
	{
		super.handleMoveArrived();
		String walkerId = getOwner().getSpawn().getWalkerId();
		if (walkerId != null)
		{
			template = DataManager.WALKER_DATA.getWalkerTemplate(walkerId);
		}
		int point = getOwner().getMoveController().getCurrentPoint();
		if (template.getRouteSteps().size() - 1 ==  point)
		{
			getSpawnTemplate().setWalkerId(null);
			WalkManager.stopWalking(this);
			canThink = true;
			addHate();
		}
	}
}