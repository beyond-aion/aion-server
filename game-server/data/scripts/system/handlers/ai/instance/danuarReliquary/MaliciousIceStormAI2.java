package ai.instance.danuarReliquary;

import com.aionemu.commons.network.util.ThreadPoolManager;
import com.aionemu.gameserver.ai2.AI2Actions;
import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.ai2.NpcAI2;


/**
 * @author Ritsu
 *
 */
@AIName("malicious_ice_storm")
public class MaliciousIceStormAI2 extends NpcAI2 
{

	@Override
	protected void handleSpawned()
	{
		super.handleSpawned();
		iceStorm();
	}

	private void iceStorm() 
	{
		ThreadPoolManager.getInstance().schedule(new Runnable()
		{

			@Override
			public void run() 
			{
				AI2Actions.useSkill(MaliciousIceStormAI2.this, 21180);
				despawn();
			}
		}, 100);
	}


	private void despawn()
	{
		ThreadPoolManager.getInstance().schedule(new Runnable() 
		{

			@Override
			public void run() 
			{
				getOwner().getController().onDelete();
			}
		}, 2100);
	}
}


