package ai.instance.abyssal_splinter;

import ai.AggressiveNpcAI2;

import com.aionemu.gameserver.ai2.AIName;
import com.aionemu.gameserver.utils.ThreadPoolManager;


/**
 * @author Ritsu
 */

@AIName("yamenessportal")
public class YamenessPortalSummonedAI2 extends AggressiveNpcAI2
{

	@Override
	protected void handleSpawned()
	{
		ThreadPoolManager.getInstance().schedule(new Runnable() 
		{
			@Override
			public void run() 
			{
				spawnSummons();
			}
		}, 12000);
	}

	private void spawnSummons() 
	{
		if (getOwner() != null) 
		{
			spawn(281903, getOwner().getX() + 3, getOwner().getY() - 3, getOwner().getZ(), (byte) 0);
			spawn(281904, getOwner().getX() - 3, getOwner().getY() + 3, getOwner().getZ(), (byte) 0);
			ThreadPoolManager.getInstance().schedule(new Runnable() 
			{
				@Override
				public void run() 
				{
					if(!isAlreadyDead() && getOwner() != null)
					{
						spawn(281903, getOwner().getX() + 3, getOwner().getY() - 3, getOwner().getZ(), (byte) 0);
						spawn(281904, getOwner().getX() - 3, getOwner().getY() + 3, getOwner().getZ(), (byte) 0);
					}
				}
			}, 60000);
		}
	}
}