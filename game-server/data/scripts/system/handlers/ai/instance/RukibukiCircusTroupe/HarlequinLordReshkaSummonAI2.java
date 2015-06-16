package ai.instance.RukibukiCircusTroupe;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.model.actions.NpcActions;
import com.aionemu.gameserver.model.gameobjects.Creature;
import com.aionemu.gameserver.model.gameobjects.Npc;

/**
 *
 * @author Ritsu
 */
@AIName("harlequinlordreshkasummon")
public class HarlequinLordReshkaSummonAI2 extends AggressiveNpcAI2
{

	private Npc boss;

	@Override
	protected void handleSpawned() 
	{
		boss = getPosition().getWorldMapInstance().getNpc(233453);
		if (boss != null && !NpcActions.isAlreadyDead(boss))
		{
			Creature player = boss.getPosition().getWorldMapInstance().getNpc(233453).getAggroList().getMostHated();
			getAggroList().addHate(player, 1);
		}	
		super.handleSpawned();
	}
}